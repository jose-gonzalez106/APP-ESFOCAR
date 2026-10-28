package com.example.caso_semestral.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.caso_semestral.ColorDocente
import com.example.caso_semestral.R
import com.example.caso_semestral.VerdeInstitucional
import com.example.caso_semestral.model.Rol
import com.example.caso_semestral.viewmodel.AuthViewModel
import com.example.caso_semestral.viewmodel.DocumentosViewModel
import com.example.caso_semestral.viewmodel.EstadoSesion
import kotlinx.coroutines.launch

private data class Pestana(val ruta: String, val titulo: String, val icono: ImageVector)

// el docente ve "Material" en vez de "Documentos"
private fun pestanasPara(docente: Boolean) = listOf(
    Pestana(Rutas.INICIO, "Inicio", Icons.Default.Home),
    Pestana(Rutas.DOCUMENTOS, if (docente) "Material" else "Documentos", Icons.Default.Folder),
    Pestana(Rutas.PERFIL, "Perfil", Icons.Default.Person)
)

private fun NavHostController.irAPestana(ruta: String) {
    navigate(ruta) {
        popUpTo(Rutas.INICIO) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

// ---------- Transiciones entre pantallas ----------
// Registro y Visor entran deslizándose desde la derecha; el resto (pestañas, login) con desvanecimiento
private const val DURACION = 300

private fun String?.deslizante() = this == Rutas.VISOR || this == Rutas.REGISTRO || this == Rutas.LOGIN

private fun AnimatedContentTransitionScope<NavBackStackEntry>.entrada(): EnterTransition =
    if (targetState.destination.route.deslizante())
        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(DURACION)) + fadeIn(tween(DURACION))
    else fadeIn(tween(DURACION))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.salida(): ExitTransition =
    if (targetState.destination.route.deslizante())
        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(DURACION)) + fadeOut(tween(DURACION))
    else fadeOut(tween(DURACION))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.entradaAtras(): EnterTransition =
    if (initialState.destination.route.deslizante())
        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(DURACION)) + fadeIn(tween(DURACION))
    else fadeIn(tween(DURACION))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.salidaAtras(): ExitTransition =
    if (initialState.destination.route.deslizante())
        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(DURACION)) + fadeOut(tween(DURACION))
    else fadeOut(tween(DURACION))

@Composable
fun AppRoot(
    authVm: AuthViewModel = viewModel(),
    docVm: DocumentosViewModel = viewModel()
) {
    val estado by authVm.estadoSesion.collectAsState()
    val rol by authVm.rol.collectAsState()

    // mientras se lee la sesión guardada (y el rol de la cuenta) no se muestra ninguna pantalla:
    // evita el parpadeo del login y que un docente vea un instante la pantalla de estudiante
    if (estado is EstadoSesion.Cargando || (estado is EstadoSesion.ConSesion && rol == null)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        ContenidoApp(estado, rol ?: Rol.ESTUDIANTE, authVm, docVm)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenidoApp(
    estado: EstadoSesion,
    rol: Rol,                       // NUEVO
    authVm: AuthViewModel,
    docVm: DocumentosViewModel
) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val ruta = backStack?.destination?.route

    val seleccionado by docVm.seleccionado.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val alias by authVm.alias.collectAsState()
    val asignaturas by authVm.asignaturas.collectAsState()   // NUEVO
    val foto by authVm.foto.collectAsState()                 // NUEVO: foto de perfil
    val documentos by docVm.documentos.collectAsState()
    val mensaje by docVm.mensaje.collectAsState()
    var confirmarCierre by remember { mutableStateOf(false) }

    // NUEVO: cada rol tiene su color, su menú y sus pantallas
    val esDocente = rol == Rol.DOCENTE
    val colorRol = if (esDocente) ColorDocente else VerdeInstitucional
    val pestanas = remember(esDocente) { pestanasPara(esDocente) }

    val usuarioId = (estado as? EstadoSesion.ConSesion)?.usuarioId
    // se calcula una sola vez: cambiarlo después reiniciaría el NavHost
    val destinoInicial = remember { if (usuarioId != null) Rutas.INICIO else Rutas.BIENVENIDA }

    // reacciona a iniciar o cerrar sesión
    LaunchedEffect(usuarioId) {
        docVm.establecerUsuario(usuarioId)
        val actual = nav.currentDestination?.route ?: return@LaunchedEffect
        if (usuarioId != null && actual in Rutas.publicas) {
            nav.navigate(Rutas.INICIO) { popUpTo(nav.graph.id) { inclusive = true } }
        } else if (usuarioId == null && actual !in Rutas.publicas) {
            nav.navigate(Rutas.BIENVENIDA) { popUpTo(nav.graph.id) { inclusive = true } }
        }
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it)
            docVm.mensajeMostrado()
        }
    }

    val conBarraSuperior = ruta != null && ruta !in Rutas.publicas
    val conBarraInferior = ruta in Rutas.pestanas
    val titulo = pestanas.find { it.ruta == ruta }?.titulo
        ?: seleccionado?.nombre?.substringAfter("_")
        ?: "Documento"
    var tituloBarra by remember { mutableStateOf("") }
    LaunchedEffect(titulo, conBarraSuperior) { if (conBarraSuperior) tituloBarra = titulo }
    ModalNavigationDrawer(
        drawerState = drawer,
        gesturesEnabled = conBarraSuperior,
        drawerContent = {
            ModalDrawerSheet {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo_esfocar),
                        contentDescription = "Logo Escuela de Formación de Carabineros",
                        modifier = Modifier.height(48.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (esDocente) "ESFOCAR Docente" else "ESFOCAR Estudio",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                if (alias.isNotEmpty()) {
                    Text(
                        "Nombre: $alias",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                pestanas.forEach { p ->
                    NavigationDrawerItem(
                        label = { Text(p.titulo) },
                        icon = { Icon(p.icono, contentDescription = null) },
                        selected = ruta == p.ruta,
                        onClick = {
                            scope.launch { drawer.close() }
                            nav.irAPestana(p.ruta)
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawer.close() }
                        confirmarCierre = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Text(
                    "Logo: Escuela de Formación de Carabineros. Uso académico (MVP con datos ficticios).",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                AnimatedVisibility(
                    visible = conBarraSuperior,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit = slideOutVertically { -it } + fadeOut()
                ) {
                    TopAppBar(
                        title = {
                            AnimatedContent(targetState = tituloBarra, label = "tituloBarra") { t -> Text(t) }
                        },
                        navigationIcon = {
                            if (ruta == Rutas.VISOR) {
                                IconButton(onClick = { nav.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                                }
                            } else {
                                IconButton(onClick = { scope.launch { drawer.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = colorRol,                      // CAMBIADO
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White
                        )
                    )
                }
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = conBarraInferior,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    NavigationBar {
                        pestanas.forEach { p ->
                            NavigationBarItem(
                                selected = ruta == p.ruta,
                                onClick = { nav.irAPestana(p.ruta) },
                                icon = { Icon(p.icono, contentDescription = null) },
                                label = { Text(p.titulo) },
                                // NUEVO: el docente ve la selección en dorado
                                colors = if (esDocente) {
                                    NavigationBarItemDefaults.colors(
                                        selectedIconColor = ColorDocente,
                                        selectedTextColor = ColorDocente,
                                        indicatorColor = ColorDocente.copy(alpha = 0.15f)
                                    )
                                } else {
                                    NavigationBarItemDefaults.colors()
                                }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = destinoInicial,
                modifier = Modifier.padding(padding),
                enterTransition = { entrada() },
                exitTransition = { salida() },
                popEnterTransition = { entradaAtras() },
                popExitTransition = { salidaAtras() }
            ) {
                // NUEVO: primera pantalla, elegir estudiante o docente
                composable(Rutas.BIENVENIDA) {
                    BienvenidaScreen(
                        onEstudiante = { nav.navigate(Rutas.login(Rol.ESTUDIANTE)) },
                        onDocente = { nav.navigate(Rutas.login(Rol.DOCENTE)) }
                    )
                }
                composable(
                    Rutas.LOGIN,
                    arguments = listOf(navArgument("rol") { type = NavType.StringType })
                ) { entry ->
                    val rolElegido = Rutas.rolDe(entry.arguments?.getString("rol"))
                    LoginScreen(
                        vm = authVm,
                        rol = rolElegido,
                        onIrARegistro = { nav.navigate(Rutas.registro(rolElegido)) },
                        onVolver = { nav.popBackStack() }
                    )
                }
                composable(
                    Rutas.REGISTRO,
                    arguments = listOf(navArgument("rol") { type = NavType.StringType })
                ) { entry ->
                    val rolElegido = Rutas.rolDe(entry.arguments?.getString("rol"))
                    RegistroScreen(authVm, rolInicial = rolElegido) { nav.popBackStack() }
                }
                // CAMBIADO: según el rol se muestra un Inicio distinto
                composable(Rutas.INICIO) {
                    if (esDocente) {
                        InicioDocenteScreen(
                            alias = alias,
                            asignaturas = asignaturas,
                            documentos = documentos,
                            onIrDocumentos = { nav.irAPestana(Rutas.DOCUMENTOS) },
                            onAbrir = { id -> nav.navigate(Rutas.visor(id)) }
                        )
                    } else {
                        InicioScreen(
                            alias = alias,
                            documentos = documentos,
                            onIrDocumentos = { nav.irAPestana(Rutas.DOCUMENTOS) },
                            onAbrir = { id -> nav.navigate(Rutas.visor(id)) }
                        )
                    }
                }
                // CAMBIADO: recibe el rol y las asignaturas
                composable(Rutas.DOCUMENTOS) {
                    DocumentosScreen(
                        vm = docVm,
                        esDocente = esDocente,
                        asignaturas = asignaturas,
                        onAbrir = { id -> nav.navigate(Rutas.visor(id)) }
                    )
                }
                composable(
                    Rutas.VISOR,
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { entrada ->
                    VisorScreen(docVm, entrada.arguments?.getInt("id") ?: -1)
                }
                // CAMBIADO: el perfil recibe rol y asignaturas
                composable(Rutas.PERFIL) {
                    PerfilScreen(
                        alias = alias,
                        rol = rol,
                        asignaturas = asignaturas,
                        fotoRuta = foto,
                        onCambiarFoto = { uri -> authVm.cambiarFoto(uri) },
                        onQuitarFoto = { authVm.quitarFoto() },
                        onCerrarSesion = { confirmarCierre = true }
                    )
                }
            }
        }
    }

    if (confirmarCierre) {
        AlertDialog(
            onDismissRequest = { confirmarCierre = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Quieres cerrar tu sesión? Tus documentos seguirán guardados en el teléfono.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarCierre = false
                    authVm.cerrarSesion()
                }) { Text("Cerrar sesión") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarCierre = false }) { Text("Cancelar") }
            }
        )
    }
}
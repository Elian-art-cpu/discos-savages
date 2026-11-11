package org.elian;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Main {

    static String[][] ListaUsuarios = new String[1000][4];
    static int contadorUsuarios = 0;

    static String[][] ListaInventario = new String[1000][9];
    static int contadorInventario = 0;

    static Scanner scanner = new Scanner(System.in);
    static boolean sesionIniciada = false;
    static String usuarioLogueadoCorreo = "";
    static String usuarioLogueadoNombre = "";
    static boolean EsAdmin = false;

    public static void main(String[] args) {
        precargarDatosBase();

        int opcion = -1;
        do {
            limpiarPantalla();
            System.out.println("=======================================");
            System.out.println("     SISTEMA DE GESTIÓN - DISCOS SALVAJES");
            System.out.println("=======================================");

            if (!sesionIniciada) {
                System.out.println("1. Registrarse");
                System.out.println("2. Iniciar Sesión");
                System.out.println("0. Salir");
                System.out.print("Seleccione una opción: ");

                opcion = leerEntero();
                switch (opcion) {
                    case 1: registrarUsuario(); break;
                    case 2: iniciarSesion(); break;
                    case 0: System.out.println("¡Gracias por usar Discos Salvajes!"); break;
                    default:
                        System.out.println("[Error] Opción no válida.");
                        pausar();
                }
            } else {
                System.out.println("Sesión activa: " + usuarioLogueadoNombre + " (" + (EsAdmin ? "Administrador" : "Cliente") + ")");
                System.out.println("---------------------------------------");
                System.out.println("1. Ver Catálogo / Inventario");
                System.out.println("2. Ordenar Inventario");
                if (EsAdmin) {
                    System.out.println("3. [ADMIN] Registrar Nuevo Álbum");
                    System.out.println("4. [ADMIN] Ver Lista de Usuarios y Perfiles");
                }
                System.out.println("5. Cerrar Sesión");
                System.out.println("0. Salir del Sistema");
                System.out.print("Seleccione una opción: ");

                opcion = leerEntero();
                switch (opcion) {
                    case 1:
                        mostrarInventario();
                        pausar();
                        break;
                    case 2:
                        menuOrdenamiento();
                        break;
                    case 3:
                        if (EsAdmin) {
                            registrarAlbum();
                        } else {
                            System.out.println("[Error] Acceso denegado. Se requiere rol de Administrador.");
                        }
                        pausar();
                        break;
                    case 4:
                        if (EsAdmin) {
                            gestionarUsuariosAdmin();
                        } else {
                            System.out.println("[Error] Acceso denegado. Se requiere rol de Administrador.");
                        }
                        pausar();
                        break;
                    case 5:
                        cerrarSesion();
                        break;
                    case 0:
                        System.out.println("¡Fin del programa!");
                        break;
                    default:
                        System.out.println("[Error] Opción no válida.");
                        pausar();
                }
            }
        } while (opcion != 0);
    }

    // ==========================================
    // MÓDULO DE SEGURIDAD Y AUTENTICACIÓN
    // ==========================================
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error de criptografía", e);
        }
    }

    public static void registrarUsuario() {
        limpiarPantalla();
        System.out.println("--- MÓDULO DE REGISTRO ---");
        System.out.print("Ingrese su nombre: ");
        String nombre = scanner.nextLine().trim();

        System.out.print("Ingrese su correo: ");
        String correo = scanner.nextLine().trim();

        if (nombre.isEmpty() || correo.isEmpty()) {
            System.out.println("[Error] Los campos no pueden estar vacíos.");
            pausar();
            return;
        }

        for (int i = 0; i < contadorUsuarios; i++) {
            if (ListaUsuarios[i][1].equalsIgnoreCase(correo)) {
                System.out.println("[Error] Este correo ya se encuentra registrado.");
                pausar();
                return;
            }
        }

        System.out.println("Seleccione su rango:");
        System.out.println("1. Usuario / Cliente");
        System.out.println("2. Administrador");
        System.out.print("Opción: ");
        String rango = scanner.nextLine().trim();
        while (!rango.equals("1") && !rango.equals("2")) {
            System.out.println("Rango incorrecto. Ingrese 1 o 2:");
            rango = scanner.nextLine().trim();
        }

        System.out.print("Ingrese su contraseña: ");
        String contraseña = scanner.nextLine().trim();

        if (contraseña.isEmpty()) {
            System.out.println("[Error] La contraseña no puede estar vacía.");
            pausar();
            return;
        }

        if (contadorUsuarios < ListaUsuarios.length) {
            ListaUsuarios[contadorUsuarios][0] = nombre;
            ListaUsuarios[contadorUsuarios][1] = correo;
            ListaUsuarios[contadorUsuarios][2] = rango;
            ListaUsuarios[contadorUsuarios][3] = hashPassword(contraseña);
            contadorUsuarios++;
            System.out.println("¡Usuario registrado con éxito!");
        }
        pausar();
    }

    public static void iniciarSesion() {
        limpiarPantalla();
        System.out.println("--- MÓDULO DE INICIO DE SESIÓN ---");
        System.out.print("Ingrese su correo: ");
        String correoIngresado = scanner.nextLine().trim();

        System.out.print("Ingrese su contraseña: ");
        String contraseñaIngresada = scanner.nextLine().trim();

        if (correoIngresado.isEmpty() || contraseñaIngresada.isEmpty()) {
            System.out.println("[Error] Los campos no pueden estar vacíos.");
            pausar();
            return;
        }

        boolean usuarioEncontrado = false;
        String hashIngresado = hashPassword(contraseñaIngresada);

        for (int i = 0; i < contadorUsuarios; i++) {
            if (ListaUsuarios[i][1].equalsIgnoreCase(correoIngresado)) {
                usuarioEncontrado = true;
                if (ListaUsuarios[i][3].equals(hashIngresado)) {
                    sesionIniciada = true;
                    usuarioLogueadoCorreo = ListaUsuarios[i][1];
                    usuarioLogueadoNombre = ListaUsuarios[i][0];
                    EsAdmin = ListaUsuarios[i][2].equals("2");
                    System.out.println("¡Inicio de sesión exitoso! Bienvenido, " + usuarioLogueadoNombre);
                } else {
                    System.out.println("[Error] Usuario o contraseña incorrectos.");
                }
                break;
            }
        }

        if (!usuarioEncontrado) {
            System.out.println("[Error] Usuario o contraseña incorrectos.");
        }
        pausar();
    }

    public static void cerrarSesion() {
        sesionIniciada = false;
        usuarioLogueadoCorreo = "";
        usuarioLogueadoNombre = "";
        EsAdmin = false;
        System.out.println("Sesión cerrada correctamente.");
        pausar();
    }

    // ==========================================
    // MÓDULO DE INVENTARIO
    // ==========================================
    public static void registrarAlbum() {
        limpiarPantalla();
        System.out.println("--- MÓDULO DE REGISTRO DE ÁLBUM ---");

        System.out.print("Título del álbum: ");
        String titulo = scanner.nextLine().trim();

        System.out.print("Artista: ");
        String artista = scanner.nextLine().trim();

        System.out.print("Género: ");
        String genero = scanner.nextLine().trim();

        System.out.print("Subgénero: ");
        String subgenero = scanner.nextLine().trim();

        System.out.print("Año de publicación: ");
        String anio = scanner.nextLine().trim();

        System.out.println("Estado del disco (Mint, Near Mint, VG+, VG, G): ");
        String estado = scanner.nextLine().trim();
        while (!estado.equalsIgnoreCase("Mint") &&
                !estado.equalsIgnoreCase("Near Mint") &&
                !estado.equalsIgnoreCase("VG+") &&
                !estado.equalsIgnoreCase("VG") &&
                !estado.equalsIgnoreCase("G")) {
            System.out.println("[Error] Estado inválido. Ingrese uno correcto (Mint, Near Mint, VG+, VG, G):");
            estado = scanner.nextLine().trim();
        }

        System.out.print("Precio ($): ");
        String precio = scanner.nextLine().trim();

        System.out.print("Formato Físico (Ej. Vinilo LP 12\", Gatefold): ");
        String formato = scanner.nextLine().trim();

        System.out.print("Cantidad en stock: ");
        String cantidad = scanner.nextLine().trim();

        if (contadorInventario < ListaInventario.length) {
            ListaInventario[contadorInventario][0] = titulo;
            ListaInventario[contadorInventario][1] = artista;
            ListaInventario[contadorInventario][2] = genero;
            ListaInventario[contadorInventario][3] = subgenero;
            ListaInventario[contadorInventario][4] = anio;
            ListaInventario[contadorInventario][5] = estado.toUpperCase();
            ListaInventario[contadorInventario][6] = precio;
            ListaInventario[contadorInventario][7] = formato;
            ListaInventario[contadorInventario][8] = cantidad;

            contadorInventario++;
            System.out.println("¡Álbum registrado correctamente!");
        } else {
            System.out.println("[Error] Inventario lleno.");
        }
    }

    public static void mostrarInventario() {
        limpiarPantalla();
        System.out.println("==================================================================================");
        System.out.println("                              CATÁLOGO DE DISCOS                                  ");
        System.out.println("==================================================================================");
        if (contadorInventario == 0) {
            System.out.println("No hay discos registrados en el inventario actualmente.");
            return;
        }

        System.out.printf("%-20s | %-15s | %-10s | %-10s | %-6s | %-10s | %-8s | %-5s\n",
                "Título", "Artista", "Género", "Subgénero", "Año", "Estado", "Precio", "Stock");
        System.out.println("----------------------------------------------------------------------------------");
        for (int i = 0; i < contadorInventario; i++) {
            System.out.printf("%-20s | %-15s | %-10s | %-10s | %-6s | %-10s | $%-7s | %-5s\n",
                    recortar(ListaInventario[i][0], 20),
                    recortar(ListaInventario[i][1], 15),
                    recortar(ListaInventario[i][2], 10),
                    recortar(ListaInventario[i][3], 10),
                    ListaInventario[i][4],
                    ListaInventario[i][5],
                    ListaInventario[i][6],
                    ListaInventario[i][8]);
        }
        System.out.println("==================================================================================");
    }

    // ==========================================
    // MÓDULO DE ORDENAMIENTO
    // ==========================================
    public static void menuOrdenamiento() {
        limpiarPantalla();
        System.out.println("--- MÓDULO DE ORDENAMIENTO ---");
        System.out.println("Seleccione el criterio de ordenamiento:");
        System.out.println("1. Nombre / Título");
        System.out.println("2. Género / Subgénero");
        System.out.println("3. Cronológico (Año)");
        System.out.println("4. Estado");
        System.out.println("5. Precio");
        System.out.print("Opción: ");

        int criterio = leerEntero();
        if (criterio < 1 || criterio > 5) {
            System.out.println("[Error] Criterio inválido.");
            pausar();
            return;
        }

        System.out.println("Seleccione la dirección:");
        System.out.println("1. Ascendente");
        System.out.println("2. Descendente");
        System.out.print("Opción (Por defecto Ascendente): ");
        int dir = leerEntero();
        boolean ascendente = (dir != 2);

        for (int i = 0; i < contadorInventario - 1; i++) {
            for (int j = 0; j < contadorInventario - i - 1; j++) {
                String valA = obtenerValorOrden(j, criterio);
                String valB = obtenerValorOrden(j + 1, criterio);

                int comparacion = valA.compareToIgnoreCase(valB);
                if (!ascendente) comparacion = -comparacion;

                if (comparacion > 0) {
                    for (int k = 0; k < 9; k++) {
                        String temp = ListaInventario[j][k];
                        ListaInventario[j][k] = ListaInventario[j + 1][k];
                        ListaInventario[j + 1][k] = temp;
                    }
                }
            }
        }
        System.out.println("\n¡Inventario ordenado con éxito!");
        mostrarInventario();
        pausar();
    }

    public static String obtenerValorOrden(int indice, int criterio) {
        switch (criterio) {
            case 1: return ListaInventario[indice][0];
            case 2: return ListaInventario[indice][2];
            case 3: return ListaInventario[indice][4];
            case 4: return ListaInventario[indice][5];
            case 5:
                try {
                    return String.format("%010d", Integer.parseInt(ListaInventario[indice][6]));
                } catch (Exception e) {
                    return "0000000000";
                }
            default: return "";
        }
    }

    // ==========================================
    // MÓDULO DE GESTIÓN DE USUARIOS
    // ==========================================
    public static void gestionarUsuariosAdmin() {
        limpiarPantalla();
        System.out.println("--- GESTIÓN DE USUARIOS REGISTRADOS ---");
        System.out.printf("%-20s | %-25s | %-15s\n", "Nombre", "Correo", "Rol");
        System.out.println("---------------------------------------------------------------");
        for (int i = 0; i < contadorUsuarios; i++) {
            String rolStr = ListaUsuarios[i][2].equals("2") ? "Administrador" : "Cliente";
            System.out.printf("%-20s | %-25s | %-15s\n",
                    recortar(ListaUsuarios[i][0], 20),
                    recortar(ListaUsuarios[i][1], 25),
                    rolStr);
        }
        System.out.println("---------------------------------------------------------------");
    }

    // ==========================================
    // MÉTODOS AUXILIARES CORREGIDOS PARA INTELLIJ
    // ==========================================
    public static void precargarDatosBase() {
        ListaUsuarios[0][0] = "Administrador Principal";
        ListaUsuarios[0][1] = "admin@vinilos.com";
        ListaUsuarios[0][2] = "2";
        ListaUsuarios[0][3] = hashPassword("Admin123*");
        contadorUsuarios++;

        ListaInventario[0] = new String[]{"Abbey Road", "The Beatles", "Rock", "Classic Rock", "1969", "Mint", "450", "Vinilo LP", "5"};
        ListaInventario[1] = new String[]{"Thriller", "Michael Jackson", "Pop", "Dance-Pop", "1982", "Near Mint", "350", "Vinilo LP", "3"};
        contadorInventario = 2;
    }

    // Método seguro para leer enteros sin romper el ciclo ni dejar la consola colgada
    public static int leerEntero() {
        while (true) {
            if (scanner.hasNextInt()) {
                int val = scanner.nextInt();
                scanner.nextLine(); // Limpia el enter pendiente (\n)
                return val;
            } else {
                System.out.print("Entrada inválida. Ingrese un número válido: ");
                scanner.next(); // Descarta la entrada errónea
            }
        }
    }

    // Limpieza de pantalla segura para IntelliJ (imprime saltos en lugar de comandos de sistema)
    public static void limpiarPantalla() {
        for (int i = 0; i < 30; i++) {
            System.out.println();
        }
    }

    // Pausa segura que espera a que el usuario presione Enter
    public static void pausar() {
        System.out.print("\nPresione ENTER para continuar...");
        scanner.nextLine();
    }

    public static String recortar(String texto, int max) {
        if (texto == null) return "";
        if (texto.length() <= max) return texto;
        return texto.substring(0, max - 3) + "...";
    }
}
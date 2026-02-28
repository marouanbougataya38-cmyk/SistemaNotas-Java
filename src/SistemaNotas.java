import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SistemaNotas {
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + "/users.txt";
    private static final String USUARIOS_DIR = DATA_DIR + "/usuarios";
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        crearDirectorios();
        boolean salir = false;

        while (!salir) {
            mostrarMenuPrincipal();
            int opcion = leerOpcion();
            switch (opcion) {
                case 1 -> registrarUsuario();
                case 2 -> iniciarSesion();
                case 3 -> salir = true;
                default -> System.out.println("❌ Opción inválida.");
            }
        }
        System.out.println("¡Gracias por usar el sistema de notas!");
    }

    private static void crearDirectorios() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Files.createDirectories(Paths.get(USUARIOS_DIR));
        } catch (IOException e) {
            System.err.println("Error creando directorios: " + e.getMessage());
        }
    }

    private static void mostrarMenuPrincipal() {
        System.out.println("\n" + "=".repeat(30));
        System.out.println("     MENÚ PRINCIPAL");
        System.out.println("=".repeat(30));
        System.out.println("1. Registrarse");
        System.out.println("2. Iniciar sesión");
        System.out.println("3. Salir");
        System.out.print("Elige una opción: ");
    }

    private static int leerOpcion() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void registrarUsuario() {
        System.out.println("\n" + "=".repeat(30));
        System.out.println("          REGISTRO");
        System.out.println("=".repeat(30));

        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        System.out.print("Contraseña: ");
        String pass = sc.nextLine().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            System.out.println(" Error: Email y contraseña no pueden estar vacíos.");
            return;
        }
        if (usuarioExiste(email)) {
            System.out.println(" Error: El usuario ya existe.");
            return;
        }

        String sanitized = sanitizarEmail(email);
        Path userDir = Paths.get(USUARIOS_DIR, sanitized);

        try {
            Files.createDirectories(userDir);

            String linea = email + ":" + pass;
            Files.write(Paths.get(USERS_FILE),
                    (linea + System.lineSeparator()).getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            System.out.println(" Usuario registrado correctamente.");
        } catch (IOException e) {
            System.out.println(" Error al registrar: " + e.getMessage());
        }
    }

    private static boolean usuarioExiste(String email) {
        if (!Files.exists(Paths.get(USERS_FILE))) return false;
        try (BufferedReader br = Files.newBufferedReader(Paths.get(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(email + ":")) return true;
            }
        } catch (IOException ignored) {}
        return false;
    }

    private static String sanitizarEmail(String email) {
        return email.replace("@", "_")
                    .replace(".", "_")
                    .replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private static void iniciarSesion() {
        System.out.println("\n" + "=".repeat(30));
        System.out.println("       INICIO DE SESIÓN");
        System.out.println("=".repeat(30));

        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        System.out.print("Contraseña: ");
        String pass = sc.nextLine().trim();

        if (!verificarCredenciales(email, pass)) {
            System.out.println(" Usuario o contraseña incorrectos.");
            return;
        }

        System.out.println(" ¡Bienvenido, " + email + "!");
        menuUsuario(email);
    }

    private static boolean verificarCredenciales(String email, String pass) {
        if (!Files.exists(Paths.get(USERS_FILE))) return false;
        try (BufferedReader br = Files.newBufferedReader(Paths.get(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] partes = line.split(":", 2);
                if (partes.length == 2 && partes[0].equals(email) && partes[1].equals(pass)) {
                    return true;
                }
            }
        } catch (IOException ignored) {}
        return false;
    }

    private static void menuUsuario(String email) {
        String sanitized = sanitizarEmail(email);
        Path userDir = Paths.get(USUARIOS_DIR, sanitized);
        Path notesPath = userDir.resolve("notas.txt");

        boolean sesionActiva = true;
        while (sesionActiva) {
            mostrarMenuUsuario();
            int opcion = leerOpcion();
            switch (opcion) {
                case 1 -> crearNota(notesPath);
                case 2 -> listarNotas(notesPath);
                case 3 -> verNota(notesPath);
                case 4 -> eliminarNota(notesPath);
                case 5 -> {
                    sesionActiva = false;
                    System.out.println("👋 Sesión cerrada.");
                }
                default -> System.out.println(" Opción inválida.");
            }
        }
    }

    private static void mostrarMenuUsuario() {
        System.out.println("\n" + "=".repeat(30));
        System.out.println("       MENÚ DE USUARIO");
        System.out.println("=".repeat(30));
        System.out.println("1. Crear nota");
        System.out.println("2. Listar notas");
        System.out.println("3. Ver nota por número");
        System.out.println("4. Eliminar nota (por número)");
        System.out.println("5. Cerrar sesión");
        System.out.print("Elige una opción: ");
    }

    private static void crearNota(Path notesPath) {
        System.out.println("\n CREAR NOTA ");
        System.out.print("Título: ");
        String titulo = sc.nextLine().trim();
        System.out.print("Contenido: ");
        String contenido = sc.nextLine().trim();

        if (titulo.isEmpty() || contenido.isEmpty()) {
            System.out.println(" Título y contenido no pueden estar vacíos.");
            return;
        }

        String linea = titulo + ";" + contenido;
        try {
            Files.write(notesPath,
                    (linea + System.lineSeparator()).getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            System.out.println("✅ Nota guardada.");
        } catch (IOException e) {
            System.out.println(" Error: " + e.getMessage());
        }
    }

    private static void listarNotas(Path notesPath) {
        System.out.println("\n=== LISTAR NOTAS ===");
        if (!Files.exists(notesPath)) {
            System.out.println(" No hay notas aún.");
            return;
        }
        try (BufferedReader br = Files.newBufferedReader(notesPath)) {
            String line;
            int num = 1;
            boolean hayNotas = false;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] partes = line.split(";", 2);
                    if (partes.length == 2) {
                        System.out.printf("%d.  %s%n    %s%n", num, partes[0], partes[1]);
                        System.out.println("-".repeat(40));
                        num++;
                        hayNotas = true;
                    }
                }
            }
            if (!hayNotas) System.out.println(" No hay notas.");
        } catch (IOException e) {
            System.out.println(" Error al leer notas: " + e.getMessage());
        }
    }

    private static void verNota(Path notesPath) {
        System.out.println("\n=== VER NOTA ===");
        if (!Files.exists(notesPath)) {
            System.out.println(" No hay notas.");
            return;
        }
        System.out.print("Número de la nota: ");
        int num = leerOpcion();
        if (num < 1) {
            System.out.println(" Número inválido.");
            return;
        }

        try (BufferedReader br = Files.newBufferedReader(notesPath)) {
            String line;
            int actual = 1;
            while ((line = br.readLine()) != null) {
                if (actual == num) {
                    String[] partes = line.split(";", 2);
                    if (partes.length == 2) {
                        System.out.println(" Título: " + partes[0]);
                        System.out.println(" Contenido: " + partes[1]);
                        return;
                    }
                }
                actual++;
            }
            System.out.println(" Nota no encontrada.");
        } catch (IOException e) {
            System.out.println(" Error: " + e.getMessage());
        }
    }

    private static void eliminarNota(Path notesPath) {
        System.out.println("\n ELIMINAR NOTA ");
        if (!Files.exists(notesPath)) {
            System.out.println(" No hay notas.");
            return;
        }
        System.out.print("Número de nota a eliminar: ");
        int num = leerOpcion();
        if (num < 1) {
            System.out.println(" Número inválido.");
            return;
        }

        List<String> lineas = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(notesPath)) {
            String line;
            while ((line = br.readLine()) != null) {
                lineas.add(line);
            }
        } catch (IOException e) {
            System.out.println(" Error al leer: " + e.getMessage());
            return;
        }

        if (num > lineas.size()) {
            System.out.println(" Número fuera de rango.");
            return;
        }

        lineas.remove(num - 1);
        try {
            Files.write(notesPath, lineas);
            System.out.println("  Nota eliminada correctamente.");
        } catch (IOException e) {
            System.out.println(" Error al guardar: " + e.getMessage());
        }
    }
}

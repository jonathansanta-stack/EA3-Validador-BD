import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ValidadorConexionBD extends JFrame {

    private JComboBox<String> cmbMotor;
    private JTextField txtHost;
    private JTextField txtPuerto;
    private JTextField txtBaseDatos;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnProbar;
    private JButton btnLimpiar;
    private JTextArea txtResultado;

    public ValidadorConexionBD() {
        setTitle("EA3 - Validador de Conexión JDBC");
        setSize(520, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        inicializarComponentes();
    }

    private void inicializarComponentes() {
        // Panel de formulario
        JPanel panelForm = new JPanel(new GridLayout(6, 2, 8, 8));
        panelForm.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        panelForm.add(new JLabel("Motor de Base de Datos:"));
        cmbMotor = new JComboBox<>(new String[]{"MySQL", "PostgreSQL"});
        cmbMotor.addActionListener(e -> actualizarPuertoPorDefecto());
        panelForm.add(cmbMotor);

        panelForm.add(new JLabel("Host / Servidor:"));
        txtHost = new JTextField("localhost");
        panelForm.add(txtHost);

        panelForm.add(new JLabel("Puerto:"));
        txtPuerto = new JTextField("3306");
        panelForm.add(txtPuerto);

        panelForm.add(new JLabel("Nombre de la Base de Datos:"));
        txtBaseDatos = new JTextField();
        panelForm.add(txtBaseDatos);

        panelForm.add(new JLabel("Usuario:"));
        txtUsuario = new JTextField("root");
        panelForm.add(txtUsuario);

        panelForm.add(new JLabel("Contraseña:"));
        txtPassword = new JPasswordField();
        panelForm.add(txtPassword);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        btnProbar = new JButton("Probar Conexión");
        btnLimpiar = new JButton("Limpiar");

        btnProbar.addActionListener(e -> probarConexion());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        panelBotones.add(btnProbar);
        panelBotones.add(btnLimpiar);

        // Panel superior combinado
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(panelForm, BorderLayout.CENTER);
        panelSuperior.add(panelBotones, BorderLayout.SOUTH);

        // Área de resultados / logs JDBC
        txtResultado = new JTextArea();
        txtResultado.setEditable(false);
        txtResultado.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtResultado.setLineWrap(true);
        txtResultado.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(txtResultado);
        scroll.setBorder(BorderFactory.createTitledBorder("Estado y Diagnóstico JDBC"));

        add(panelSuperior, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void actualizarPuertoPorDefecto() {
        String motor = (String) cmbMotor.getSelectedItem();
        if ("MySQL".equals(motor)) {
            txtPuerto.setText("3306");
        } else if ("PostgreSQL".equals(motor)) {
            txtPuerto.setText("5432");
        }
    }

    private void probarConexion() {
        String motor = (String) cmbMotor.getSelectedItem();
        String host = txtHost.getText().trim();
        String puerto = txtPuerto.getText().trim();
        String bd = txtBaseDatos.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        // Validación básica de campos obligatorios
        if (host.isEmpty() || puerto.isEmpty() || bd.isEmpty() || usuario.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, completa Host, Puerto, Base de Datos y Usuario.",
                    "Campos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String url;
        String driver;

        if ("MySQL".equals(motor)) {
            driver = "com.mysql.cj.jdbc.Driver";
            url = "jdbc:mysql://" + host + ":" + puerto + "/" + bd + "?connectTimeout=5000";
        } else {
            driver = "org.postgresql.Driver";
            url = "jdbc:postgresql://" + host + ":" + puerto + "/" + bd + "?connectTimeout=5";
        }

        txtResultado.setText("Intentando conectar a:\n" + url + "\n\n");
        DriverManager.setLoginTimeout(5);

        try {
            // 1. Cargar el driver JDBC
            Class.forName(driver);

            // 2. Intentar establecer la conexión
            try (Connection conn = DriverManager.getConnection(url, usuario, password)) {
                if (conn != null && !conn.isClosed()) {
                    txtResultado.append("✔ CONEXIÓN EXITOSA\n");
                    txtResultado.append("----------------------------------------\n");
                    txtResultado.append("Motor: " + conn.getMetaData().getDatabaseProductName() + "\n");
                    txtResultado.append("Versión: " + conn.getMetaData().getDatabaseProductVersion() + "\n");
                    txtResultado.append("Driver: " + conn.getMetaData().getDriverName() + "\n");

                    JOptionPane.showMessageDialog(this,
                            "Conexión establecida correctamente.",
                            "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            }

        } catch (ClassNotFoundException ex) {
            txtResultado.append("✖ ERROR: Driver JDBC no encontrado.\n");
            txtResultado.append("Detalle: " + ex.getMessage() + "\n");
            txtResultado.append("Solución: Agrega el archivo .jar del conector (" + motor + ") a las librerías del proyecto.\n");

            JOptionPane.showMessageDialog(this,
                    "No se encontró el driver JDBC para " + motor + ".",
                    "Error de Driver", JOptionPane.ERROR_MESSAGE);

        } catch (SQLException ex) {
            txtResultado.append("✖ ERROR DE CONEXIÓN JDBC (SQLException)\n");
            txtResultado.append("----------------------------------------\n");
            txtResultado.append("Código de Error: " + ex.getErrorCode() + "\n");
            txtResultado.append("Estado SQL (SQLState): " + ex.getSQLState() + "\n");
            txtResultado.append("Mensaje: " + ex.getMessage() + "\n");

            JOptionPane.showMessageDialog(this,
                    "Error al conectar con la base de datos.\nRevisa el panel de diagnóstico.",
                    "Error JDBC", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarCampos() {
        txtBaseDatos.setText("");
        txtPassword.setText("");
        txtResultado.setText("");
    }

    public static void main(String[] args) {
       new ValidadorConexionBD().setVisible(true);
    }
}
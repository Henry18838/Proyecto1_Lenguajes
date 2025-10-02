package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import lexer.*;

public class DepuracionDialog extends JDialog {
    private JTextArea textArea;
    private JTextArea logArea;
    private JButton btnSiguiente, btnAnterior, btnPausar, btnReiniciar, btnEjecutarTodo;
    private JLabel lblEstado, lblCaracter, lblLexema;
    
    private Automata automata;
    private String texto;
    private int posicionActual;
    private boolean pausado;
    private java.util.List<String> historial;
    private int historialIndex;
    
    public DepuracionDialog(JFrame parent, String texto) {
        super(parent, "Modo Depuración - Análisis Paso a Paso", true);
        this.texto = texto;
        this.automata = new Automata();
        this.posicionActual = 0;
        this.pausado = true;
        this.historial = new java.util.ArrayList<>();
        this.historialIndex = -1;
        
        initComponents();
        setupLayout();
        setupEvents();
        
        setSize(800, 600);
        setLocationRelativeTo(parent);
        actualizarEstado();
    }
    
    private void initComponents() {
        // Área de texto de entrada
        textArea = new JTextArea(texto);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        textArea.setEditable(false);
        textArea.setBackground(new Color(240, 240, 240));
        
        // Área de log de depuración
        logArea = new JTextArea();
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setEditable(false);
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);
        
        // Botones
        btnSiguiente = new JButton("Siguiente Carácter (F8)");
        btnAnterior = new JButton("Anterior (F7)");
        btnPausar = new JButton("Pausar");
        btnReiniciar = new JButton("Reiniciar (F5)");
        btnEjecutarTodo = new JButton("Ejecutar Todo (F9)");
        
        // Configurar colores de botones
        btnSiguiente.setBackground(new Color(0, 100, 0));
        btnSiguiente.setForeground(Color.WHITE);
        btnAnterior.setBackground(new Color(100, 100, 0));
        btnAnterior.setForeground(Color.WHITE);
        btnPausar.setBackground(new Color(100, 0, 0));
        btnPausar.setForeground(Color.WHITE);
        btnReiniciar.setBackground(new Color(0, 0, 100));
        btnReiniciar.setForeground(Color.WHITE);
        btnEjecutarTodo.setBackground(new Color(0, 100, 100));
        btnEjecutarTodo.setForeground(Color.WHITE);
        
        // Etiquetas de estado
        lblEstado = new JLabel("Estado: INICIAL");
        lblCaracter = new JLabel("Carácter actual: Ninguno");
        lblLexema = new JLabel("Lexema actual: ");
        
        lblEstado.setFont(new Font("Arial", Font.BOLD, 14));
        lblCaracter.setFont(new Font("Arial", Font.BOLD, 12));
        lblLexema.setFont(new Font("Arial", Font.BOLD, 12));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // === PANEL SUPERIOR - BOTONES ===
        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panelBotones.setBackground(new Color(220, 220, 220));
        
        panelBotones.add(btnReiniciar);
        panelBotones.add(btnAnterior);
        panelBotones.add(btnSiguiente);
        panelBotones.add(btnPausar);
        panelBotones.add(btnEjecutarTodo);
        
        // === PANEL ESTADO ===
        JPanel panelEstado = new JPanel(new GridLayout(1, 3));
        panelEstado.setBorder(BorderFactory.createTitledBorder("Estado Actual"));
        panelEstado.add(lblEstado);
        panelEstado.add(lblCaracter);
        panelEstado.add(lblLexema);
        
        // === PANEL SUPERIOR COMBINADO ===
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(panelBotones, BorderLayout.NORTH);
        panelSuperior.add(panelEstado, BorderLayout.CENTER);
        
        // === PANEL TEXTO ENTRADA ===
        JPanel panelTexto = new JPanel(new BorderLayout());
        panelTexto.setBorder(BorderFactory.createTitledBorder("Texto de Entrada"));
        panelTexto.add(new JScrollPane(textArea), BorderLayout.CENTER);
        
        // === PANEL LOG ===
        JPanel panelLog = new JPanel(new BorderLayout());
        panelLog.setBorder(BorderFactory.createTitledBorder("Log de Depuración"));
        panelLog.add(new JScrollPane(logArea), BorderLayout.CENTER);
        
        // === DIVISOR PRINCIPAL ===
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelTexto, panelLog);
        splitPane.setDividerLocation(200);
        
        // === AGREGAR TODO ===
        add(panelSuperior, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }
    
    private void setupEvents() {
        // Botón Siguiente
        btnSiguiente.addActionListener(e -> siguientePaso());
        
        // Botón Anterior
        btnAnterior.addActionListener(e -> pasoAnterior());
        
        // Botón Pausar
        btnPausar.addActionListener(e -> togglePausa());
        
        // Botón Reiniciar
        btnReiniciar.addActionListener(e -> reiniciar());
        
        // Botón Ejecutar Todo
        btnEjecutarTodo.addActionListener(e -> ejecutarTodo());
        
        // Atajos de teclado
        setupAtajosTeclado();
    }
    
    private void setupAtajosTeclado() {
        // F8 - Siguiente
        KeyStroke f8 = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f8, "siguiente");
        getRootPane().getActionMap().put("siguiente", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (pausado) siguientePaso();
            }
        });
        
        // F7 - Anterior
        KeyStroke f7 = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f7, "anterior");
        getRootPane().getActionMap().put("anterior", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                pasoAnterior();
            }
        });
        
        // F5 - Reiniciar
        KeyStroke f5 = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f5, "reiniciar");
        getRootPane().getActionMap().put("reiniciar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                reiniciar();
            }
        });
        
        // F9 - Ejecutar Todo
        KeyStroke f9 = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f9, "ejecutarTodo");
        getRootPane().getActionMap().put("ejecutarTodo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ejecutarTodo();
            }
        });
    }
    
    private void siguientePaso() {
        if (posicionActual >= texto.length()) {
            agregarLog("=== ANÁLISIS COMPLETADO ===");
            pausado = true;
            actualizarBotones();
            return;
        }
        
        char caracter = texto.charAt(posicionActual);
        
        // Saltar espacios y tabs en depuración
        if (caracter == ' ' || caracter == '\t' || caracter == '\r') {
            agregarLog("Saltando carácter: '" + (caracter == ' ' ? "ESPACIO" : "TAB") + "'");
            posicionActual++;
            siguientePaso(); // Recursivo para saltar múltiples espacios
            return;
        }
        
        // Guardar estado actual en historial
        guardarEnHistorial();
        
        // Procesar carácter
        int estadoAnterior = automata.getCurrentState();
        int nuevoEstado = automata.transition(caracter);
        
        // Mostrar información en log
        String mensaje = String.format("Carácter: '%c' | Estado: %s → %s | Lexema: '%s'", 
            caracter, 
            obtenerNombreEstado(estadoAnterior),
            obtenerNombreEstado(nuevoEstado),
            automata.getCurrentLexeme());
        agregarLog(mensaje);
        
        // Resaltar carácter actual en el texto
        resaltarCaracterActual();
        
        posicionActual++;
        actualizarEstado();
        
        // Si llegamos a un estado final o error, pausar automáticamente
        if (automata.isFinalState() || automata.isErrorState()) {
            if (automata.isFinalState()) {
                agregarLog(">>> ESTADO FINAL ALCANZADO - Token: " + automata.getTokenType());
            } else {
                agregarLog(">>> ERROR - No hay transición posible");
            }
            pausado = true;
            actualizarBotones();
        }
    }
    
    private void pasoAnterior() {
        if (historialIndex > 0) {
            historialIndex--;
            restaurarDesdeHistorial();
            actualizarEstado();
            resaltarCaracterActual();
        } else {
            agregarLog("No hay pasos anteriores");
        }
    }
    
    private void togglePausa() {
        pausado = !pausado;
        actualizarBotones();
        if (!pausado) {
            ejecutarAutomatico();
        }
    }
    
    private void ejecutarAutomatico() {
        if (!pausado && posicionActual < texto.length()) {
            siguientePaso();
            if (!pausado) {
                Timer timer = new Timer(500, e -> ejecutarAutomatico()); // 500ms entre pasos
                timer.setRepeats(false);
                timer.start();
            }
        } else {
            pausado = true;
            actualizarBotones();
        }
    }
    
    private void ejecutarTodo() {
        pausado = false;
        actualizarBotones();
        new Thread(() -> {
            while (!pausado && posicionActual < texto.length()) {
                siguientePaso();
                try {
                    Thread.sleep(100); // 100ms entre pasos
                } catch (InterruptedException e) {
                    break;
                }
            }
            pausado = true;
            SwingUtilities.invokeLater(this::actualizarBotones);
        }).start();
    }
    
    private void reiniciar() {
        automata.reset();
        posicionActual = 0;
        pausado = true;
        historial.clear();
        historialIndex = -1;
        logArea.setText("");
        textArea.setSelectionStart(0);
        textArea.setSelectionEnd(0);
        actualizarEstado();
        actualizarBotones();
        agregarLog("=== DEPURACIÓN REINICIADA ===");
    }
    
    private void guardarEnHistorial() {
        // Guardar snapshot del estado actual
        String snapshot = posicionActual + "|" + 
                         automata.getCurrentState() + "|" + 
                         automata.getCurrentLexeme();
        
        // Limpiar historial futuro si retrocedimos y avanzamos de nuevo
        if (historialIndex < historial.size() - 1) {
            historial = historial.subList(0, historialIndex + 1);
        }
        
        historial.add(snapshot);
        historialIndex = historial.size() - 1;
    }
    
    private void restaurarDesdeHistorial() {
        if (historialIndex >= 0 && historialIndex < historial.size()) {
            String snapshot = historial.get(historialIndex);
            String[] partes = snapshot.split("\\|");
            
            posicionActual = Integer.parseInt(partes[0]);
            
            // Recrear el autómata desde el inicio
            automata.reset();
            for (int i = 0; i < posicionActual; i++) {
                char c = texto.charAt(i);
                if (c != ' ' && c != '\t' && c != '\r') {
                    automata.transition(c);
                }
            }
        }
    }
    
    private void resaltarCaracterActual() {
        if (posicionActual < texto.length()) {
            textArea.setSelectionStart(posicionActual);
            textArea.setSelectionEnd(posicionActual + 1);
            textArea.setSelectionColor(Color.YELLOW);
        } else {
            textArea.setSelectionStart(texto.length());
            textArea.setSelectionEnd(texto.length());
        }
    }
    
    private void actualizarEstado() {
        lblEstado.setText("Estado: " + obtenerNombreEstado(automata.getCurrentState()));
        
        if (posicionActual < texto.length()) {
            char caracter = texto.charAt(posicionActual);
            lblCaracter.setText("Carácter actual: '" + 
                (caracter == ' ' ? "ESPACIO" : 
                 caracter == '\t' ? "TAB" : 
                 caracter == '\n' ? "SALTO_LÍNEA" : 
                 String.valueOf(caracter)) + "'");
        } else {
            lblCaracter.setText("Carácter actual: FIN DEL TEXTO");
        }
        
        lblLexema.setText("Lexema actual: '" + automata.getCurrentLexeme() + "'");
    }
    
    private void actualizarBotones() {
        btnSiguiente.setEnabled(pausado && posicionActual < texto.length());
        btnAnterior.setEnabled(pausado && historialIndex > 0);
        btnPausar.setText(pausado ? "Continuar" : "Pausar");
        btnPausar.setBackground(pausado ? new Color(0, 100, 0) : new Color(100, 0, 0));
        btnEjecutarTodo.setEnabled(pausado);
    }
    
    private void agregarLog(String mensaje) {
        logArea.append(mensaje + "\n");
        // Auto-scroll al final
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    
    private String obtenerNombreEstado(int estado) {
        switch (estado) {
            case Automata.STATE_INITIAL: return "INICIAL";
            case Automata.STATE_IDENTIFIER: return "IDENTIFICADOR";
            case Automata.STATE_INTEGER: return "ENTERO";
            case Automata.STATE_DECIMAL_POINT: return "PUNTO_DECIMAL";
            case Automata.STATE_DECIMAL: return "DECIMAL";
            case Automata.STATE_STRING: return "CADENA";
            case Automata.STATE_OPERATOR: return "OPERADOR";
            case Automata.STATE_SLASH: return "BARRA";
            case Automata.STATE_LINE_COMMENT: return "COMENTARIO_LÍNEA";
            case Automata.STATE_BLOCK_COMMENT: return "COMENTARIO_BLOQUE";
            case Automata.STATE_BLOCK_COMMENT_END: return "FIN_COMENTARIO_BLOQUE";
            case Automata.STATE_ERROR: return "ERROR";
            default: return "DESCONOCIDO";
        }
    }
}
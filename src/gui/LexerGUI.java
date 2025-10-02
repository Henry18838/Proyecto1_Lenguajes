package gui;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class LexerGUI extends JFrame {
    // Componentes de la interfaz
    private JTextPane textPane;
    private JTextArea resultArea;
    private JLabel statusLabel;
    private JButton analyzeBtn, loadBtn, saveBtn, searchBtn, debugBtn, exportReportBtn, diagramBtn;
    private JTextField searchField;
    private Timer timerResaltado;
    
    public LexerGUI() {
        initComponents();
        setupLayout();
        setupEvents();
    }
    
    private void initComponents() {
        setTitle("Analizador Léxico - Proyecto 1");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null); // Centrar en la pantalla
        
        // JTextPane para soportar resaltado
        textPane = new JTextPane();
        textPane.setFont(new Font("Consolas", Font.PLAIN, 14));
        
        // Configurar tabulación para JTextPane
        TabStop[] tabs = new TabStop[20];
        for (int i = 0; i < tabs.length; i++) {
            tabs[i] = new TabStop((i + 1) * 72, TabStop.ALIGN_LEFT, TabStop.LEAD_NONE);
        }
        TabSet tabSet = new TabSet(tabs);
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.TabSet, tabSet);
        textPane.setParagraphAttributes(aset, false);
        
        // Área de texto para resultados
        resultArea = new JTextArea();
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(240, 240, 240));
        
        // Etiqueta de estado
        statusLabel = new JLabel("Listo - Fila: 1, Columna: 1");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());
        
        // Botones
        analyzeBtn = new JButton("Analizar");
        loadBtn = new JButton("Cargar");
        saveBtn = new JButton("Guardar");
        searchBtn = new JButton("Buscar");
        debugBtn = new JButton("Depurar");
        exportReportBtn = new JButton("Exportar");
        diagramBtn = new JButton("Diagrama");
        searchField = new JTextField(15);
        
        // Configurar colores de botones
        analyzeBtn.setBackground(new Color(70, 130, 180));
        analyzeBtn.setForeground(Color.WHITE);
        loadBtn.setBackground(new Color(34, 139, 34));
        loadBtn.setForeground(Color.WHITE);
        debugBtn.setBackground(new Color(255, 140, 0));
        debugBtn.setForeground(Color.WHITE);
        exportReportBtn.setBackground(new Color(75, 0, 130));
        exportReportBtn.setForeground(Color.WHITE);
        diagramBtn.setBackground(new Color(128, 0, 128));
        diagramBtn.setForeground(Color.WHITE);
        
        // Timer para resaltado en tiempo real
        timerResaltado = new Timer(500, e -> aplicarResaltadoSintaxis());
        timerResaltado.setRepeats(false); // Solo ejecutar una vez después del delay
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // ===== PANEL SUPERIOR CON BOTONES =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPanel.setBackground(new Color(230, 230, 230));
        
        // Primera fila de botones (archivos y búsqueda)
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        row1.setBackground(new Color(230, 230, 230));
        row1.add(loadBtn);
        row1.add(saveBtn);
        row1.add(Box.createHorizontalStrut(20));
        row1.add(new JLabel("Buscar:"));
        row1.add(searchField);
        row1.add(searchBtn);
        
        // Segunda fila de botones (análisis y herramientas)
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        row2.setBackground(new Color(230, 230, 230));
        row2.add(analyzeBtn);
        row2.add(debugBtn);
        row2.add(exportReportBtn);
        row2.add(diagramBtn);
        
        // Panel contenedor para ambas filas
        JPanel buttonContainer = new JPanel();
        buttonContainer.setLayout(new BoxLayout(buttonContainer, BoxLayout.Y_AXIS));
        buttonContainer.setBackground(new Color(230, 230, 230));
        buttonContainer.add(row1);
        buttonContainer.add(row2);
        
        topPanel.add(buttonContainer, BorderLayout.NORTH);
        
        // ===== PANEL PRINCIPAL DIVIDIDO =====
        // Panel superior (entrada de texto) CON NÚMEROS DE LÍNEA
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Texto de Entrada"));
        
        // Crear scroll pane con números de línea - USAR textPane
        JScrollPane textScroll = new JScrollPane(textPane);
        textScroll.setRowHeaderView(new LineNumberPanel(textPane));
        textScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        inputPanel.add(textScroll, BorderLayout.CENTER);
        
        // Panel inferior (resultados)
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Resultados del Análisis"));
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        resultPanel.add(resultScroll, BorderLayout.CENTER);
        
        // Divisor
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, resultPanel);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.6);
        
        // ===== AGREGAR TODO A LA VENTANA =====
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }
    
    private void setupEvents() {
        // Botón cargar archivo
        loadBtn.addActionListener(e -> {
            loadFile();
            SwingUtilities.invokeLater(() -> aplicarResaltadoSintaxis());
        });
        
        // Botón guardar archivo
        saveBtn.addActionListener(e -> saveFile());
        
        // Botón analizar
        analyzeBtn.addActionListener(e -> {
            analyzeText();
            aplicarResaltadoSintaxis();
        });
        
        // Botón buscar
        searchBtn.addActionListener(e -> searchText());
        
        // Botón depuración
        debugBtn.addActionListener(e -> abrirDepuracion());
        
        // Botón exportar reporte
        exportReportBtn.addActionListener(e -> exportarReporte());
        
        // Botón diagrama del autómata
        diagramBtn.addActionListener(e -> mostrarDiagramaAutomata());
        
        // Actualizar posición del cursor en tiempo real
        textPane.addCaretListener(e -> updateCursorPosition());
        
        // Resaltado en tiempo real
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                timerResaltado.restart();
            }
            public void removeUpdate(DocumentEvent e) {
                timerResaltado.restart();
            }
            public void changedUpdate(DocumentEvent e) {
                timerResaltado.restart();
            }
        });
        
        // Atajos de teclado
        setupKeyboardShortcuts();
    }
    
    private void aplicarResaltadoSintaxis() {
        try {
            String texto = textPane.getText();
            if (texto.isEmpty()) return;
            
            // Crear analizador para obtener tokens
            lexer.Lexer lexer = new lexer.Lexer();
            lexer.analyze(texto);
            
            // Aplicar resaltado
            aplicarColoresPorTokens(lexer.getTokens());
            
        } catch (Exception ex) {
            // Silenciar errores durante el resaltado en tiempo real
            System.out.println("Error en resaltado: " + ex.getMessage());
        }
    }
    
    private void aplicarColoresPorTokens(java.util.List<lexer.Token> tokens) {
        // Obtener el documento del JTextPane (que SÍ soporta estilos)
        StyledDocument doc = textPane.getStyledDocument();
        
        try {
            // Limpiar todos los estilos previos - establecer color negro por defecto
            Style defaultStyle = doc.addStyle("Default", null);
            StyleConstants.setForeground(defaultStyle, Color.BLACK);
            doc.setCharacterAttributes(0, doc.getLength(), defaultStyle, true);
            
            // Definir estilos con los colores especificados
            Style estiloReservada = doc.addStyle("RESERVADA", null);
            StyleConstants.setForeground(estiloReservada, Color.BLUE);
            StyleConstants.setBold(estiloReservada, true);
            
            Style estiloIdentificador = doc.addStyle("IDENTIFICADOR", null);
            StyleConstants.setForeground(estiloIdentificador, new Color(139, 69, 19)); // Café
            
            Style estiloEntero = doc.addStyle("ENTERO", null);
            StyleConstants.setForeground(estiloEntero, Color.GREEN);
            
            Style estiloDecimal = doc.addStyle("DECIMAL", null);
            StyleConstants.setForeground(estiloDecimal, Color.BLACK);
            
            Style estiloComentario = doc.addStyle("COMENTARIO", null);
            StyleConstants.setForeground(estiloComentario, new Color(0, 100, 0)); // Verde oscuro
            StyleConstants.setItalic(estiloComentario, true);
            
            Style estiloOperador = doc.addStyle("OPERADOR", null);
            StyleConstants.setForeground(estiloOperador, Color.ORANGE);
            
            Style estiloAgrupacion = doc.addStyle("AGRUPACION", null);
            StyleConstants.setForeground(estiloAgrupacion, new Color(128, 0, 128)); // Morado
            
            Style estiloError = doc.addStyle("ERROR", null);
            StyleConstants.setForeground(estiloError, Color.RED);
            StyleConstants.setBackground(estiloError, new Color(255, 255, 200)); // Fondo amarillo claro
            StyleConstants.setBold(estiloError, true);
            
            Style estiloCadena = doc.addStyle("CADENA", null);
            StyleConstants.setForeground(estiloCadena, new Color(163, 21, 21)); // Rojo oscuro
            
            Style estiloPuntuacion = doc.addStyle("PUNTUACION", null);
            StyleConstants.setForeground(estiloPuntuacion, new Color(128, 0, 128)); // Morado
            
            // Aplicar estilos a cada token
            for (lexer.Token token : tokens) {
                int startPos = calcularPosicionEnTexto(token.getLine(), token.getColumn(), textPane.getText());
                int length = token.getLexeme().length();
                
                if (startPos >= 0 && startPos + length <= doc.getLength()) {
                    try {
                        Style estilo = obtenerEstiloPorTipo(token.getType());
                        if (estilo != null) {
                            doc.setCharacterAttributes(startPos, length, estilo, false);
                        }
                    } catch (Exception e) {
                        System.out.println("Error aplicando estilo para: " + token.getLexeme());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error en aplicarColoresPorTokens: " + e.getMessage());
        }
    }
    
    private Style obtenerEstiloPorTipo(lexer.TokenType tipo) {
        StyledDocument doc = textPane.getStyledDocument();
        
        switch (tipo) {
            case PALABRA_RESERVADA:
                return doc.getStyle("RESERVADA");
            case IDENTIFICADOR:
                return doc.getStyle("IDENTIFICADOR");
            case ENTERO:
                return doc.getStyle("ENTERO");
            case DECIMAL:
                return doc.getStyle("DECIMAL");
            case COMENTARIO_LINEA:
            case COMENTARIO_BLOQUE:
                return doc.getStyle("COMENTARIO");
            case OPERADOR:
                return doc.getStyle("OPERADOR");
            case AGRUPACION:
                return doc.getStyle("AGRUPACION");
            case ERROR:
                return doc.getStyle("ERROR");
            case CADENA:
                return doc.getStyle("CADENA");
            case PUNTUACION:
                return doc.getStyle("PUNTUACION");
            default:
                return null;
        }
    }
    
    private int calcularPosicionEnTexto(int fila, int columna, String texto) {
        int posicion = 0;
        int lineaActual = 1;
        int columnaActual = 1;
        
        for (int i = 0; i < texto.length(); i++) {
            if (lineaActual == fila && columnaActual == columna) {
                return i;
            }
            
            if (texto.charAt(i) == '\n') {
                lineaActual++;
                columnaActual = 1;
            } else {
                columnaActual++;
            }
            posicion++;
        }
        
        return -1; // No encontrado
    }
    
    private void updateCursorPosition() {
        try {
            int caretPosition = textPane.getCaretPosition();
            int lineNumber = 1;
            int columnNumber = 1;
            
            String text = textPane.getText();
            for (int i = 0; i < caretPosition; i++) {
                if (text.charAt(i) == '\n') {
                    lineNumber++;
                    columnNumber = 1;
                } else {
                    columnNumber++;
                }
            }
            
            statusLabel.setText(String.format("Fila: %d, Columna: %d", lineNumber, columnNumber));
        } catch (Exception ex) {
            statusLabel.setText("Error obteniendo posición");
        }
    }
    
    private void loadFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar archivo de texto");
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                BufferedReader reader = new BufferedReader(new FileReader(file));
                
                textPane.setText(""); // Limpiar área de texto
                String line;
                while ((line = reader.readLine()) != null) {
                    textPane.getDocument().insertString(
                        textPane.getDocument().getLength(), 
                        line + "\n", 
                        null
                    );
                }
                
                reader.close();
                statusLabel.setText("Archivo cargado: " + file.getName());
                
                // Aplicar resaltado después de cargar
                SwingUtilities.invokeLater(() -> aplicarResaltadoSintaxis());
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error al cargar archivo: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar archivo");
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(textPane.getText());
                writer.close();
                statusLabel.setText("Archivo guardado: " + file.getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error al guardar archivo: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void searchText() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese texto a buscar");
            return;
        }
        
        String content = textPane.getText();
        int index = content.indexOf(searchText);
        
        if (index >= 0) {
            textPane.setCaretPosition(index);
            textPane.select(index, index + searchText.length());
            textPane.grabFocus();
            statusLabel.setText("Texto encontrado: " + searchText);
        } else {
            JOptionPane.showMessageDialog(this, "Texto no encontrado: " + searchText);
        }
    }
    
    private void analyzeText() {
        String inputText = textPane.getText().trim();
        if (inputText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay texto para analizar");
            return;
        }
        
        try {
            // Crear y ejecutar analizador léxico
            lexer.Lexer lexer = new lexer.Lexer();
            lexer.analyze(inputText);
            
            // Mostrar resultados
            resultArea.setText(lexer.getResults());
            
            // Actualizar estado
            java.util.List<String> errors = lexer.getErrors();
            if (errors.isEmpty()) {
                statusLabel.setText("Análisis completado - " + lexer.getTokens().size() + " tokens encontrados");
            } else {
                statusLabel.setText("Análisis completado con " + errors.size() + " errores");
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error durante el análisis: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private void abrirDepuracion() {
        String inputText = textPane.getText().trim();
        if (inputText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay texto para depurar");
            return;
        }
        
        DepuracionDialog dialog = new DepuracionDialog(this, inputText);
        dialog.setVisible(true);
    }
    
    private void exportarReporte() {
        if (resultArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay resultados para exportar");
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exportar Reporte");
        fileChooser.setSelectedFile(new File("reporte_analisis.txt"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                FileWriter writer = new FileWriter(file);
                writer.write(resultArea.getText());
                writer.close();
                statusLabel.setText("Reporte exportado: " + file.getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error al exportar reporte: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void mostrarDiagramaAutomata() {
        AutomataVisualizer.mostrarDiagrama();
    }
    
    private void setupKeyboardShortcuts() {
        // Ctrl + O para abrir archivo
        KeyStroke ctrlO = KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK);
        textPane.getInputMap().put(ctrlO, "abrirArchivo");
        textPane.getActionMap().put("abrirArchivo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                loadFile();
            }
        });
        
        // Ctrl + S para guardar
        KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);
        textPane.getInputMap().put(ctrlS, "guardarArchivo");
        textPane.getActionMap().put("guardarArchivo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                saveFile();
            }
        });
        
        // Ctrl + D para depuración
        KeyStroke ctrlD = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK);
        textPane.getInputMap().put(ctrlD, "modoDepuracion");
        textPane.getActionMap().put("modoDepuracion", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                abrirDepuracion();
            }
        });
        
        // Ctrl + R para resaltado manual
        KeyStroke ctrlR = KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK);
        textPane.getInputMap().put(ctrlR, "resaltarSintaxis");
        textPane.getActionMap().put("resaltarSintaxis", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                aplicarResaltadoSintaxis();
                statusLabel.setText("Resaltado aplicado manualmente");
            }
        });
        
        // Ctrl + E para exportar reporte
        KeyStroke ctrlE = KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);
        textPane.getInputMap().put(ctrlE, "exportarReporte");
        textPane.getActionMap().put("exportarReporte", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                exportarReporte();
            }
        });
        
       
    }
    
    public static void main(String[] args) {
        // Establecer look and feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new LexerGUI().setVisible(true);
        });
    }
}
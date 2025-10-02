package gui;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class AutomataVisualizer extends JFrame {
    private JPanel drawingPanel;
    
    public AutomataVisualizer() {
        initComponents();
        setupLayout();
    }
    
    private void initComponents() {
        setTitle("Diagrama Visual del Autómata Léxico");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                dibujarAutomata(g2d);
            }
        };
        drawingPanel.setBackground(Color.WHITE);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(new JScrollPane(drawingPanel), BorderLayout.CENTER);
        
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(240, 240, 240));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Leyenda del Autómata"));
        
        JTextArea leyenda = new JTextArea();
        leyenda.setText(obtenerLeyendaAutomata());
        leyenda.setEditable(false);
        leyenda.setFont(new Font("Consolas", Font.PLAIN, 12));
        leyenda.setBackground(new Color(240, 240, 240));
        
        infoPanel.add(leyenda);
        add(infoPanel, BorderLayout.SOUTH);
    }
    
    private void dibujarAutomata(Graphics2D g) {
        // Configuración de fuentes y colores
        g.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Definir posiciones de los estados
        Map<Integer, Point> posiciones = new HashMap<>();
        posiciones.put(0, new Point(100, 300));  // INICIAL
        posiciones.put(1, new Point(300, 200));  // IDENTIFICADOR
        posiciones.put(2, new Point(300, 400));  // ENTERO
        posiciones.put(3, new Point(450, 450));  // PUNTO_DECIMAL
        posiciones.put(4, new Point(600, 400));  // DECIMAL
        posiciones.put(5, new Point(300, 500));  // CADENA
        posiciones.put(6, new Point(500, 300));  // OPERADOR
        posiciones.put(7, new Point(700, 200));  // BARRA
        posiciones.put(8, new Point(850, 150));  // COMENTARIO_LÍNEA
        posiciones.put(9, new Point(850, 250));  // COMENTARIO_BLOQUE
        posiciones.put(10, new Point(950, 250)); // FIN_COMENTARIO_BLOQUE
        posiciones.put(99, new Point(800, 400)); // ERROR
        
        // Dibujar transiciones
        g.setColor(Color.BLUE);
        dibujarTransicion(g, posiciones.get(0), posiciones.get(1), "Letras", Color.BLUE);
        dibujarTransicion(g, posiciones.get(0), posiciones.get(2), "Dígitos", Color.GREEN);
        dibujarTransicion(g, posiciones.get(0), posiciones.get(5), "\"", Color.RED);
        dibujarTransicion(g, posiciones.get(0), posiciones.get(6), "Operadores", Color.ORANGE);
        dibujarTransicion(g, posiciones.get(0), posiciones.get(7), "/", Color.MAGENTA);
        
        // Transiciones desde IDENTIFICADOR
        dibujarTransicionCircular(g, posiciones.get(1), "Letras, Dígitos", Color.BLUE);
        
        // Transiciones desde ENTERO
        dibujarTransicionCircular(g, posiciones.get(2), "Dígitos", Color.GREEN);
        dibujarTransicion(g, posiciones.get(2), posiciones.get(3), ".", Color.BLACK);
        
        // Transiciones desde PUNTO_DECIMAL
        dibujarTransicion(g, posiciones.get(3), posiciones.get(4), "Dígitos", Color.BLACK);
        
        // Transiciones desde DECIMAL
        dibujarTransicionCircular(g, posiciones.get(4), "Dígitos", Color.BLACK);
        
        // Transiciones desde CADENA
        dibujarTransicionCircular(g, posiciones.get(5), "Cualquier carácter", Color.RED);
        dibujarTransicion(g, posiciones.get(5), posiciones.get(1), "\"", Color.RED);
        
        // Transiciones desde BARRA (comentarios)
        dibujarTransicion(g, posiciones.get(7), posiciones.get(8), "/", Color.MAGENTA);
        dibujarTransicion(g, posiciones.get(7), posiciones.get(9), "*", Color.MAGENTA);
        
        // Transiciones desde COMENTARIO_LÍNEA
        dibujarTransicionCircular(g, posiciones.get(8), "Cualquier carácter", new Color(0, 100, 0));
        
        // Transiciones desde COMENTARIO_BLOQUE
        dibujarTransicionCircular(g, posiciones.get(9), "Cualquier carácter", new Color(0, 100, 0));
        dibujarTransicion(g, posiciones.get(9), posiciones.get(10), "*", new Color(0, 100, 0));
        
        // Transiciones desde FIN_COMENTARIO_BLOQUE
        dibujarTransicion(g, posiciones.get(10), posiciones.get(1), "/", new Color(0, 100, 0));
        
        // Dibujar estados
        for (Map.Entry<Integer, Point> entry : posiciones.entrySet()) {
            dibujarEstado(g, entry.getValue(), obtenerNombreEstado(entry.getKey()), 
                         esEstadoFinal(entry.getKey()), entry.getKey() == 0);
        }
    }
    
    private void dibujarEstado(Graphics2D g, Point centro, String nombre, boolean esFinal, boolean esInicial) {
        // Dibujar círculo del estado
        int radio = 30;
        
        if (esInicial) {
            g.setColor(new Color(200, 255, 200)); // Verde claro para estado inicial
        } else if (esFinal) {
            g.setColor(new Color(255, 255, 200)); // Amarillo claro para estados finales
        } else if (nombre.equals("ERROR")) {
            g.setColor(new Color(255, 200, 200)); // Rojo claro para error
        } else {
            g.setColor(new Color(200, 200, 255)); // Azul claro para otros estados
        }
        
        g.fillOval(centro.x - radio, centro.y - radio, radio * 2, radio * 2);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawOval(centro.x - radio, centro.y - radio, radio * 2, radio * 2);
        
        // Dibujar nombre del estado
        g.setColor(Color.BLACK);
        g.drawString(nombre, centro.x - 25, centro.y + 5);
        
        // Marcar estado inicial con flecha
        if (esInicial) {
            g.setColor(Color.GREEN);
            g.fillPolygon(
                new int[]{centro.x - 50, centro.x - 30, centro.x - 30},
                new int[]{centro.y, centro.y - 10, centro.y + 10},
                3
            );
            g.setColor(Color.BLACK);
            g.drawString("INICIO", centro.x - 60, centro.y - 15);
        }
        
        // Doble círculo para estados finales
        if (esFinal) {
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(1));
            g.drawOval(centro.x - radio + 3, centro.y - radio + 3, (radio - 3) * 2, (radio - 3) * 2);
        }
    }
    
    private void dibujarTransicion(Graphics2D g, Point desde, Point hasta, String etiqueta, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(2));
        
        // Dibujar línea
        g.drawLine(desde.x + 30, desde.y, hasta.x - 30, hasta.y);
        
        // Dibujar flecha
        dibujarFlecha(g, desde, hasta);
        
        // Dibujar etiqueta
        Point medio = new Point((desde.x + hasta.x) / 2, (desde.y + hasta.y) / 2);
        g.setColor(color.darker());
        g.drawString(etiqueta, medio.x, medio.y - 10);
    }
    
    private void dibujarTransicionCircular(Graphics2D g, Point estado, String etiqueta, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(2));
        
        // Dibujar arco circular (auto-transición)
        int radio = 50;
        g.drawArc(estado.x - 15, estado.y - 65, radio, radio, 0, 360);
        
        // Dibujar flecha circular
        int xFlecha = estado.x + 20;
        int yFlecha = estado.y - 40;
        g.fillPolygon(
            new int[]{xFlecha, xFlecha - 5, xFlecha + 5},
            new int[]{yFlecha, yFlecha - 8, yFlecha - 8},
            3
        );
        
        // Dibujar etiqueta
        g.setColor(color.darker());
        g.drawString(etiqueta, estado.x - 40, estado.y - 80);
    }
    
    private void dibujarFlecha(Graphics2D g, Point desde, Point hasta) {
        // Calcular ángulo de la flecha
        double angle = Math.atan2(hasta.y - desde.y, hasta.x - desde.x);
        
        // Punto donde termina la línea y empieza la flecha
        int xFlecha = (int) (hasta.x - 30 * Math.cos(angle));
        int yFlecha = (int) (hasta.y - 30 * Math.sin(angle));
        
        // Dibujar punta de flecha
        g.fillPolygon(
            new int[]{xFlecha, (int)(xFlecha - 10 * Math.cos(angle - Math.PI/6)), 
                     (int)(xFlecha - 10 * Math.cos(angle + Math.PI/6))},
            new int[]{yFlecha, (int)(yFlecha - 10 * Math.sin(angle - Math.PI/6)), 
                     (int)(yFlecha - 10 * Math.sin(angle + Math.PI/6))},
            3
        );
    }
    
    private String obtenerNombreEstado(int estado) {
        switch (estado) {
            case 0: return "INICIAL";
            case 1: return "ID";
            case 2: return "ENTERO";
            case 3: return "P_DEC";
            case 4: return "DECIMAL";
            case 5: return "CADENA";
            case 6: return "OP";
            case 7: return "BARRA";
            case 8: return "C_LINEA";
            case 9: return "C_BLOQ";
            case 10: return "FIN_CB";
            case 99: return "ERROR";
            default: return "DESC";
        }
    }
    
    private boolean esEstadoFinal(int estado) {
        return estado == 1 || estado == 2 || estado == 4 || estado == 5 || 
               estado == 6 || estado == 8 || estado == 9 || estado == 10;
    }
    
    private String obtenerLeyendaAutomata() {
        return "LEYENDA DEL AUTÓMATA LÉXICO:\n" +
               "• ○ Estado normal\n" +
               "• ⬤ Estado final (doble círculo)\n" +
               "• 🢒 Estado inicial (flecha verde)\n" +
               "• Colores de transiciones:\n" +
               "  - AZUL: Identificadores y palabras reservadas\n" +
               "  - VERDE: Números enteros\n" +
               "  - NEGRO: Números decimales\n" +
               "  - ROJO: Cadenas de texto\n" +
               "  - NARANJA: Operadores\n" +
               "  - MAGENTA: Comentarios\n" +
               "  - VERDE OSCURO: Contenido de comentarios";
    }
    
    public static void mostrarDiagrama() {
        SwingUtilities.invokeLater(() -> {
            new AutomataVisualizer().setVisible(true);
        });
    }
}
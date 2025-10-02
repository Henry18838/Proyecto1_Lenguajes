package main;

import gui.LexerGUI;

public class Main {
    public static void main(String[] args) {
        // Este método asegura que la interfaz gráfica se cree en el hilo correcto
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                crearYMostrarGUI();
            }
        });
    }
    
    private static void crearYMostrarGUI() {
        System.out.println("Iniciando Analizador Léxico...");
        
        // Crear la ventana principal
        LexerGUI ventana = new LexerGUI();
        
        // Hacer visible la ventana
        ventana.setVisible(true);
        
        System.out.println("Interfaz gráfica cargada correctamente");
    }
}
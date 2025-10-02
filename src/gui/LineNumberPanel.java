package gui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class LineNumberPanel extends JTextArea {
    private JTextComponent textComponent;

    public LineNumberPanel(JTextComponent component) {
        this.textComponent = component;
        setBackground(new Color(240, 240, 240));
        setForeground(Color.GRAY);
        setFont(new Font("Consolas", Font.PLAIN, 12));
        setEditable(false);
        setFocusable(false);
        
        // Configurar tamaño preferido
        setPreferredSize(new Dimension(40, 0));
        
        // Actualizar números cuando el documento cambie
        component.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateLineNumbers(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateLineNumbers(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateLineNumbers(); }
        });
        
        updateLineNumbers();
    }
    
    private void updateLineNumbers() {
        try {
            Document doc = textComponent.getDocument();
            String text = doc.getText(0, doc.getLength());
            String[] lines = text.split("\n", -1);
            
            StringBuilder numbers = new StringBuilder();
            for (int i = 1; i <= lines.length; i++) {
                numbers.append(i).append("\n");
            }
            
            setText(numbers.toString());
        } catch (Exception e) {
            setText("1\n");
        }
    }
}
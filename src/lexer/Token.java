package lexer;

import java.awt.Color;

public class Token {
    private TokenType type;
    private String lexeme;
    private int row;
    private int column;
    private int length;
    
    public Token(TokenType type, String lexeme, int row, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.row = row;
        this.column = column;
        this.length = lexeme != null ? lexeme.length() : 0;
    }
    
    // Getters
    public TokenType getType() { return type; }
    public String getLexeme() { return lexeme; }
    public int getRow() { return row; }
    public int getColumn() { return column; }
    public int getLength() { return length; }
    
    // Método para obtener color según tipo de token
  // Método para obtener color según tipo de token
public Color getColor() {
    switch (type) {
        case PALABRA_RESERVADA:
            return new Color(0, 0, 255); // AZUL
        case IDENTIFICADOR:
            return new Color(139, 69, 19); // CAFÉ
        case ENTERO:
            return new Color(0, 128, 0); // VERDE
        case DECIMAL:
            return Color.BLACK; // NEGRO
        case OPERADOR:
            return Color.YELLOW; // AMARILLO
        case AGRUPACION:
            return new Color(128, 0, 128); // MORADO
        case COMENTARIO_LINEA:
        case COMENTARIO_BLOQUE:
            return new Color(0, 100, 0); // VERDE OSCURO
        case ERROR:
        case CARACTER_DESCONOCIDO:
            return Color.RED; // ROJO
        default:
            return Color.BLACK;
    }
}
    
    @Override
    public String toString() {
        return String.format("Token[%s, '%s', Fila:%d, Col:%d]", 
            type, lexeme, row, column);
    }
    
    // Método para formato de reporte
    public String toReportString() {
        return String.format("%-15s %-20s Fila:%-4d Col:%-4d", 
            type.toString(), 
            "'" + lexeme + "'", 
            row, column);
    }
}
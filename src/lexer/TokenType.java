package lexer;

public enum TokenType {
    // Tokens válidos
    PALABRA_RESERVADA,    // Palabra reservada - AZUL
    IDENTIFICADOR,        // Identificador - CAFÉ
    ENTERO,               // Número entero - VERDE
    DECIMAL,              // Decimal - NEGRO
    CADENA,               // Cadena
    PUNTUACION,           // Puntuación
    OPERADOR,             // Operador - AMARILLO
    AGRUPACION,           // Agrupación - MORADO
    COMENTARIO_LINEA,     // Comentario de línea - VERDE OSCURO
    COMENTARIO_BLOQUE,    // Comentario de bloque - VERDE OSCURO
    
    // Estados de error
    ERROR,                // Error léxico - ROJO
    CARACTER_DESCONOCIDO  // Carácter desconocido - ROJO
}
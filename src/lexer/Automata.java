package lexer;

import java.util.*;

public class Automata {
    private int currentState;
    private int startState;
    private Set<Integer> finalStates;
    private Map<Integer, Map<Character, Integer>> transitionTable;
    private StringBuilder currentLexeme;
    private List<String> transitionLog;
    
    // Estados del autómata - ACTUALIZADOS
    public static final int STATE_INITIAL = 0;
    public static final int STATE_IDENTIFIER = 1;
    public static final int STATE_INTEGER = 2;
    public static final int STATE_DECIMAL_POINT = 3;
    public static final int STATE_DECIMAL = 4;
    public static final int STATE_STRING = 5;
    public static final int STATE_OPERATOR = 6;
    public static final int STATE_SLASH = 7;           // NUEVO: Cuando encuentra '/'
    public static final int STATE_LINE_COMMENT = 8;    // NUEVO: Comentario de línea //
    public static final int STATE_BLOCK_COMMENT = 9;   // NUEVO: Comentario de bloque /*
    public static final int STATE_BLOCK_COMMENT_END = 10; // NUEVO: Cierre de bloque */
    public static final int STATE_ERROR = 99;
    
    // Palabras reservadas
    private static final Set<String> RESERVED_WORDS = Set.of(
        "SI", "ENTONCES", "PARA", "ESCRIBIR",
        "si", "entonces", "para", "escribir"
    );
    
    public Automata() {
        initializeAutomata();
        reset();
    }
    
    private void initializeAutomata() {
        finalStates = new HashSet<>(Arrays.asList(
            STATE_IDENTIFIER, STATE_INTEGER, STATE_DECIMAL, 
            STATE_STRING, STATE_OPERATOR, STATE_LINE_COMMENT,
            STATE_BLOCK_COMMENT, STATE_BLOCK_COMMENT_END  // AGREGADOS
        ));
        
        transitionTable = new HashMap<>();
        transitionLog = new ArrayList<>();
        initializeTransitions();
    }
    
    private void initializeTransitions() {
        // === ESTADO INICIAL (0) ===
        Map<Character, Integer> state0 = new HashMap<>();
        
        // Letras → Identificador
        addRange(state0, 'a', 'z', STATE_IDENTIFIER);
        addRange(state0, 'A', 'Z', STATE_IDENTIFIER);
        
        // Dígitos → Entero
        addRange(state0, '0', '9', STATE_INTEGER);
        
        // Comillas → Cadena
        state0.put('\"', STATE_STRING);
        
        // Barra → Posible comentario
        state0.put('/', STATE_SLASH);
        
        // Operadores
        state0.put('+', STATE_OPERATOR);
        state0.put('-', STATE_OPERATOR);
        state0.put('*', STATE_OPERATOR);
        state0.put('%', STATE_OPERATOR);
        state0.put('=', STATE_OPERATOR);
        
        // Puntuación
        state0.put('.', STATE_OPERATOR);
        state0.put(',', STATE_OPERATOR);
        state0.put(';', STATE_OPERATOR);
        state0.put(':', STATE_OPERATOR);
        
        // Agrupación
        state0.put('(', STATE_OPERATOR);
        state0.put(')', STATE_OPERATOR);
        state0.put('[', STATE_OPERATOR);
        state0.put(']', STATE_OPERATOR);
        state0.put('{', STATE_OPERATOR);
        state0.put('}', STATE_OPERATOR);
        
        transitionTable.put(STATE_INITIAL, state0);
        
        // === ESTADO BARRA (7) - Para comentarios ===
        Map<Character, Integer> state7 = new HashMap<>();
        state7.put('/', STATE_LINE_COMMENT);   // // → Comentario de línea
        state7.put('*', STATE_BLOCK_COMMENT);  // /* → Comentario de bloque
        // Si no es comentario, es operador división
        addRange(state7, 'a', 'z', STATE_OPERATOR);
        addRange(state7, 'A', 'Z', STATE_OPERATOR);
        addRange(state7, '0', '9', STATE_OPERATOR);
        transitionTable.put(STATE_SLASH, state7);
        
        // === ESTADO COMENTARIO DE LÍNEA (8) ===
        Map<Character, Integer> state8 = new HashMap<>();
        // En comentario de línea, todo va hasta el salto de línea
        for (char c = 0; c < 256; c++) {
            if (c != '\n') {
                state8.put(c, STATE_LINE_COMMENT);
            }
        }
        state8.put('\n', STATE_IDENTIFIER); // Salto de línea termina el comentario
        transitionTable.put(STATE_LINE_COMMENT, state8);
        
        // === ESTADO COMENTARIO DE BLOQUE (9) ===
        Map<Character, Integer> state9 = new HashMap<>();
        // En comentario de bloque, todo va hasta encontrar */
        for (char c = 0; c < 256; c++) {
            if (c != '*') {
                state9.put(c, STATE_BLOCK_COMMENT);
            }
        }
        state9.put('*', STATE_BLOCK_COMMENT_END); // * podría ser el fin
        transitionTable.put(STATE_BLOCK_COMMENT, state9);
        
        // === ESTADO FIN DE COMENTARIO DE BLOQUE (10) ===
        Map<Character, Integer> state10 = new HashMap<>();
        state10.put('/', STATE_IDENTIFIER); // */ termina el comentario
        // Si después de * no viene /, volvemos al comentario de bloque
        for (char c = 0; c < 256; c++) {
            if (c != '/') {
                state10.put(c, STATE_BLOCK_COMMENT);
            }
        }
        transitionTable.put(STATE_BLOCK_COMMENT_END, state10);
        
        // === ESTADO IDENTIFICADOR (1) ===
        Map<Character, Integer> state1 = new HashMap<>();
        addRange(state1, 'a', 'z', STATE_IDENTIFIER);
        addRange(state1, 'A', 'Z', STATE_IDENTIFIER);
        addRange(state1, '0', '9', STATE_IDENTIFIER);
        transitionTable.put(STATE_IDENTIFIER, state1);
        
        // === ESTADO ENTERO (2) ===
        Map<Character, Integer> state2 = new HashMap<>();
        addRange(state2, '0', '9', STATE_INTEGER);
        state2.put('.', STATE_DECIMAL_POINT);
        transitionTable.put(STATE_INTEGER, state2);
        
        // === ESTADO PUNTO DECIMAL (3) ===
        Map<Character, Integer> state3 = new HashMap<>();
        addRange(state3, '0', '9', STATE_DECIMAL);
        transitionTable.put(STATE_DECIMAL_POINT, state3);
        
        // === ESTADO DECIMAL (4) ===
        Map<Character, Integer> state4 = new HashMap<>();
        addRange(state4, '0', '9', STATE_DECIMAL);
        transitionTable.put(STATE_DECIMAL, state4);
        
        // === ESTADO CADENA (5) ===
        Map<Character, Integer> state5 = new HashMap<>();
        // En cadena aceptamos CUALQUIER carácter excepto comilla
        for (char c = 0; c < 256; c++) {
            if (c != '\"') {
                state5.put(c, STATE_STRING);
            }
        }
        state5.put('\"', STATE_IDENTIFIER); // Comilla cierra la cadena
        transitionTable.put(STATE_STRING, state5);
        
        // === ESTADO OPERADOR (6) ===
        Map<Character, Integer> state6 = new HashMap<>();
        // Los operadores son de un solo carácter
        transitionTable.put(STATE_OPERATOR, state6);
    }
    
    private void addRange(Map<Character, Integer> transitions, char start, char end, int state) {
        for (char c = start; c <= end; c++) {
            transitions.put(c, state);
        }
    }
    
    private void addAllValidChars(Map<Character, Integer> transitions, int state) {
        // Letras
        addRange(transitions, 'a', 'z', state);
        addRange(transitions, 'A', 'Z', state);
        // Dígitos
        addRange(transitions, '0', '9', state);
        // Símbolos permitidos
        String validChars = " .,;:+-*/%=()[]{}\t\n";
        for (char c : validChars.toCharArray()) {
            transitions.put(c, state);
        }
    }
    
    public void reset() {
        currentState = STATE_INITIAL;
        currentLexeme = new StringBuilder();
        transitionLog.clear();
    }
    
    public int transition(char c) {
        Map<Character, Integer> stateTransitions = transitionTable.get(currentState);
        String currentStateName = getStateName(currentState);
        
        if (stateTransitions != null && stateTransitions.containsKey(c)) {
            int nextState = stateTransitions.get(c);
            String nextStateName = getStateName(nextState);
            
            // Registrar la transición
            String log = String.format("Me moví del estado %s al estado %s con el carácter '%c'", 
                currentStateName, nextStateName, c);
            transitionLog.add(log);
            
            currentState = nextState;
            currentLexeme.append(c);
            return nextState;
            
        } else {
            // No hay transición para este carácter - ERROR
            String log = String.format("ERROR: No hay transición del estado %s con el carácter '%c'", 
                currentStateName, c);
            transitionLog.add(log);
            
            currentState = STATE_ERROR;
            currentLexeme.append(c);
            return STATE_ERROR;
        }
    }
    
    private String getStateName(int state) {
    switch (state) {
        case STATE_INITIAL: return "INICIAL";
        case STATE_IDENTIFIER: return "IDENTIFICADOR";
        case STATE_INTEGER: return "ENTERO";
        case STATE_DECIMAL_POINT: return "PUNTO_DECIMAL";
        case STATE_DECIMAL: return "DECIMAL";
        case STATE_STRING: return "CADENA";
        case STATE_OPERATOR: return "OPERADOR";
        case STATE_SLASH: return "BARRA";
        case STATE_LINE_COMMENT: return "COMENTARIO_LÍNEA";
        case STATE_BLOCK_COMMENT: return "COMENTARIO_BLOQUE";
        case STATE_BLOCK_COMMENT_END: return "FIN_COMENTARIO_BLOQUE";
        case STATE_ERROR: return "ERROR";
        default: return "DESCONOCIDO";
    }
}
    
    public boolean isFinalState() {
        return finalStates.contains(currentState);
    }
    
    public boolean isErrorState() {
        return currentState == STATE_ERROR;
    }
    
    public int getCurrentState() { return currentState; }
    public String getCurrentLexeme() { return currentLexeme.toString(); }
    public List<String> getTransitionLog() { return new ArrayList<>(transitionLog); }
    
    public TokenType getTokenType() {
    String lexeme = currentLexeme.toString();
    
    switch (currentState) {
        case STATE_IDENTIFIER:
            return RESERVED_WORDS.contains(lexeme) ? TokenType.PALABRA_RESERVADA : TokenType.IDENTIFICADOR;
            
        case STATE_INTEGER:
            return TokenType.ENTERO;
            
        case STATE_DECIMAL:
            return TokenType.DECIMAL;
            
        case STATE_STRING:
            return TokenType.CADENA;
            
        case STATE_OPERATOR:
            if (".,;:".contains(lexeme)) {
                return TokenType.PUNTUACION;
            } else if ("()[]{}".contains(lexeme)) {
                return TokenType.AGRUPACION;
            } else {
                return TokenType.OPERADOR;
            }
            
        // NUEVOS CASOS PARA COMENTARIOS:
        case STATE_LINE_COMMENT:
            return TokenType.COMENTARIO_LINEA;
            
        case STATE_BLOCK_COMMENT:
        case STATE_BLOCK_COMMENT_END:
            return TokenType.COMENTARIO_BLOQUE;
            
        case STATE_ERROR:
            return TokenType.ERROR;
            
        default:
            return TokenType.CARACTER_DESCONOCIDO;
    }
}
    
    public boolean shouldContinue(char c) {
        // Caracteres que no reinician el autómata (espacios, tabs, etc.)
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }
}
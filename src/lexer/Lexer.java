package lexer;

import java.util.*;

public class Lexer {
    private List<Token> tokens;
    private List<String> errores;
    private List<String> logAnalisis;
    private Map<String, Integer> conteoLexemas;
    private Map<String, Integer> sugerenciasAplicadas;
    
    public Lexer() {
        tokens = new ArrayList<>();
        errores = new ArrayList<>();
        logAnalisis = new ArrayList<>();
        conteoLexemas = new HashMap<>();
        sugerenciasAplicadas = new HashMap<>();
    }
    
    public void analyze(String textoEntrada) {
        // Reiniciar listas
        tokens.clear();
        errores.clear();
        logAnalisis.clear();
        conteoLexemas.clear();
        sugerenciasAplicadas.clear();
        
        logAnalisis.add("=== INICIANDO ANÁLISIS LÉXICO ===");
        
        if (textoEntrada == null || textoEntrada.trim().isEmpty()) {
            logAnalisis.add("Texto de entrada vacío");
            return;
        }
        
        // Preprocesar: manejar comentarios primero
        String textoProcesado = preprocesarComentarios(textoEntrada);
        
        Automata automata = new Automata();
        int fila = 1;
        int columna = 1;
        int columnaInicio = 1;
        
        StringBuilder lexemaActual = new StringBuilder();
        boolean enToken = false;
        
        for (int i = 0; i < textoProcesado.length(); i++) {
            char caracterActual = textoProcesado.charAt(i);
            
            // Manejo de filas y columnas
            if (caracterActual == '\n') {
                if (enToken) {
                    procesarLexemaConLog(automata, lexemaActual, fila, columnaInicio);
                    automata.reset();
                    lexemaActual.setLength(0);
                    enToken = false;
                }
                fila++;
                columna = 1;
                columnaInicio = 1;
                continue;
            }
            
            // Ignorar espacios y tabs
            if (caracterActual == ' ' || caracterActual == '\t' || caracterActual == '\r') {
                if (enToken) {
                    procesarLexemaConLog(automata, lexemaActual, fila, columnaInicio);
                    automata.reset();
                    lexemaActual.setLength(0);
                    enToken = false;
                }
                columna++;
                continue;
            }
            
            // Procesar carácter con el autómata
            if (!enToken) {
                columnaInicio = columna;
                enToken = true;
            }
            
            int resultado = automata.transition(caracterActual);
            lexemaActual.append(caracterActual);
            
            if (automata.isErrorState()) {
                // Error encontrado - CON SUGERENCIAS MEJORADAS
                String lexemaError = lexemaActual.toString();
                
                String sugerencia = generarSugerencia(lexemaError, caracterActual, fila, columnaInicio);
                String sugerenciaContexto = obtenerSugerenciaContexto(textoProcesado, i);
                
                String mensajeError = String.format("ERROR en Fila %d, Columna %d: '%s'\n   %s\n   %s", 
                    fila, columnaInicio, lexemaError, sugerencia, sugerenciaContexto);
                
                errores.add(mensajeError);
                logAnalisis.add(mensajeError);
                
                // Mostrar movimientos del autómata para el error
                List<String> transiciones = automata.getTransitionLog();
                if (!transiciones.isEmpty()) {
                    logAnalisis.add("--- Movimientos del autómata (ERROR):");
                    for (String transicion : transiciones) {
                        logAnalisis.add("  " + transicion);
                    }
                }
                
                Token tokenError = new Token(TokenType.ERROR, lexemaError, fila, columnaInicio);
                tokens.add(tokenError);
                contarLexema(lexemaError, TokenType.ERROR);
                
                automata.reset();
                lexemaActual.setLength(0);
                enToken = false;
                
            } else if (automata.isFinalState()) {
                // Verificar si es el final de un token
                boolean esFinDeToken = esFinDeToken(i, textoProcesado, automata, caracterActual);
                
                if (esFinDeToken) {
                    procesarLexemaConLog(automata, lexemaActual, fila, columnaInicio);
                    automata.reset();
                    lexemaActual.setLength(0);
                    enToken = false;
                }
            }
            
            columna++;
        }
        
        // Procesar último lexema si queda
        if (enToken) {
            procesarLexemaConLog(automata, lexemaActual, fila, columnaInicio);
        }
        
        logAnalisis.add("=== ANÁLISIS COMPLETADO ===");
        logAnalisis.add("Total tokens: " + tokens.size());
        logAnalisis.add("Total errores: " + errores.size());
        if (!sugerenciasAplicadas.isEmpty()) {
            logAnalisis.add("Sugerencias aplicadas: " + sugerenciasAplicadas.size());
        }
    }
    
    private void procesarLexemaConLog(Automata automata, StringBuilder lexema, int fila, int columna) {
        if (lexema.length() > 0) {
            String lexemaStr = lexema.toString();
            if (!lexemaStr.trim().isEmpty()) {
                // Mostrar movimientos del autómata
                List<String> transiciones = automata.getTransitionLog();
                if (!transiciones.isEmpty()) {
                    logAnalisis.add("--- Movimientos del autómata para: '" + lexemaStr + "'");
                    for (String transicion : transiciones) {
                        logAnalisis.add("  " + transicion);
                    }
                }
                
                // Simular procesamiento final del autómata
                automata.reset();
                for (char c : lexemaStr.toCharArray()) {
                    automata.transition(c);
                }
                
                if (!automata.isErrorState() && automata.isFinalState()) {
                    TokenType tipo = automata.getTokenType();
                    
                    // No procesar si ya es un comentario
                    if (tipo != TokenType.COMENTARIO_LINEA && tipo != TokenType.COMENTARIO_BLOQUE) {
                        Token token = new Token(tipo, lexemaStr, fila, columna);
                        tokens.add(token);
                        contarLexema(lexemaStr, tipo);
                        
                        // Registrar en log
                        String mensajeLog = String.format("Token reconocido: %s '%s' en Fila %d, Columna %d", 
                            tipo, lexemaStr, fila, columna);
                        logAnalisis.add(mensajeLog);
                    }
                } else {
                    // Error en el lexema - CON SUGERENCIAS
                    String sugerencia = generarSugerencia(lexemaStr, lexemaStr.charAt(0), fila, columna);
                    String sugerenciaContexto = obtenerSugerenciaContexto(lexemaStr, 0);
                    
                    String mensajeError = String.format("ERROR en Fila %d, Columna %d: '%s'\n   %s\n   %s", 
                        fila, columna, lexemaStr, sugerencia, sugerenciaContexto);
                    
                    errores.add(mensajeError);
                    logAnalisis.add(mensajeError);
                    
                    Token tokenError = new Token(TokenType.ERROR, lexemaStr, fila, columna);
                    tokens.add(tokenError);
                    contarLexema(lexemaStr, TokenType.ERROR);
                }
                
                if (!transiciones.isEmpty()) {
                    logAnalisis.add("--- Fin de movimientos");
                }
            }
        }
    }
    
    // MÉTODOS PARA SUGERENCIAS DE CORRECCIÓN
    private String generarSugerencia(String lexemaError, char caracterError, int fila, int columna) {
        // Sugerencias basadas en el lexema
        if (lexemaError.matches(".*[0-9].*[a-zA-Z].*")) {
            contarSugerencia("identificador_con_numero");
            return "Sugerencia: Los identificadores no pueden empezar con números";
        }
        if (lexemaError.contains("'")) {
            contarSugerencia("comilla_simple");
            return "Sugerencia: Use comillas dobles \\\" para cadenas";
        }
        if (lexemaError.equals("whil") || lexemaError.equals("els")) {
            contarSugerencia("palabra_reservada_incompleta");
            return "Sugerencia: ¿Quiso decir '" + lexemaError + "e'? (Palabra reservada incompleta)";
        }
        if (lexemaError.equals("tru")) {
            contarSugerencia("palabra_reservada_incompleta");
            return "Sugerencia: ¿Quiso decir 'true'? (Palabra reservada incompleta)";
        }
        if (lexemaError.equals("fals")) {
            contarSugerencia("palabra_reservada_incompleta");
            return "Sugerencia: ¿Quiso decir 'false'? (Palabra reservada incompleta)";
        }
        if (lexemaError.matches(".*[a-zA-Z].*[0-9].*")) {
            contarSugerencia("numero_en_identificador");
            return "Sugerencia: Los números no pueden estar dentro de identificadores";
        }
        
        // Sugerencias basadas en caracteres específicos
        if ("{}[]()".indexOf(caracterError) >= 0) {
            contarSugerencia("simbolo_agrupacion");
            return "Sugerencia: Verifique los símbolos de agrupación";
        }
        if (caracterError == ';' || caracterError == ':') {
            contarSugerencia("puntuacion");
            return "Sugerencia: Revise el uso de puntuación";
        }
        
        // Sugerencias generales
        if (lexemaError.length() == 1 && !Character.isLetterOrDigit(caracterError)) {
            contarSugerencia("caracter_especial");
            return "Sugerencia: Carácter especial no reconocido";
        }
        
        contarSugerencia("general");
        return "Sugerencia: Revise la sintaxis del lexema";
    }
    
    private String obtenerSugerenciaContexto(String textoEntrada, int posicionError) {
        // Analizar el contexto alrededor del error
        if (posicionError < textoEntrada.length() - 1) {
            char siguienteChar = textoEntrada.charAt(posicionError + 1);
            if (siguienteChar == '=') {
                contarSugerencia("operador_comparacion");
                return "Sugerencia Contexto: ¿Estaba intentando usar un operador de comparación? (==, !=, etc.)";
            }
        }
        
        // Verificar si hay un comentario mal formado
        if (posicionError > 0 && textoEntrada.charAt(posicionError - 1) == '/') {
            contarSugerencia("comentario_mal_formado");
            return "Sugerencia Contexto: ¿Estaba intentando hacer un comentario? Use // o /* */";
        }
        
        // Verificar si es un operador mal formado
        if (posicionError > 0) {
            char anteriorChar = textoEntrada.charAt(posicionError - 1);
            if ("+-*/%=<>!&|".indexOf(anteriorChar) >= 0) {
                contarSugerencia("operador_mal_formado");
                return "Sugerencia Contexto: ¿Estaba intentando usar un operador compuesto? (&&, ||, ==, etc.)";
            }
        }
        
        contarSugerencia("contexto_general");
        return "Sugerencia Contexto: Verifique el contexto del código";
    }
    
    private void contarSugerencia(String tipoSugerencia) {
        sugerenciasAplicadas.put(tipoSugerencia, 
            sugerenciasAplicadas.getOrDefault(tipoSugerencia, 0) + 1);
    }
    
    private String preprocesarComentarios(String textoEntrada) {
        StringBuilder resultado = new StringBuilder();
        StringBuilder salida = new StringBuilder();
        boolean enComentarioLinea = false;
        boolean enComentarioBloque = false;
        int fila = 1;
        int columna = 1;
        
        for (int i = 0; i < textoEntrada.length(); i++) {
            char caracterActual = textoEntrada.charAt(i);
            char siguienteCaracter = (i + 1 < textoEntrada.length()) ? textoEntrada.charAt(i + 1) : '\0';
            
            // Detectar inicio de comentario de línea
            if (!enComentarioLinea && !enComentarioBloque && caracterActual == '/' && siguienteCaracter == '/') {
                enComentarioLinea = true;
                String comentario = "//";
                Token tokenComentario = new Token(TokenType.COMENTARIO_LINEA, comentario, fila, columna);
                tokens.add(tokenComentario);
                contarLexema(comentario, TokenType.COMENTARIO_LINEA);
                logAnalisis.add("Token reconocido: COMENTARIO_LINEA '//' en Fila " + fila + ", Columna " + columna);
                i++; // Saltar el siguiente '/'
                columna++;
                continue;
            }
            
            // Detectar inicio de comentario de bloque
            if (!enComentarioLinea && !enComentarioBloque && caracterActual == '/' && siguienteCaracter == '*') {
                enComentarioBloque = true;
                String comentario = "/*";
                Token tokenComentario = new Token(TokenType.COMENTARIO_BLOQUE, comentario, fila, columna);
                tokens.add(tokenComentario);
                contarLexema(comentario, TokenType.COMENTARIO_BLOQUE);
                logAnalisis.add("Token reconocido: COMENTARIO_BLOQUE '/*' en Fila " + fila + ", Columna " + columna);
                i++; // Saltar el siguiente '*'
                columna++;
                continue;
            }
            
            // Detectar fin de comentario de bloque
            if (enComentarioBloque && caracterActual == '*' && siguienteCaracter == '/') {
                enComentarioBloque = false;
                i++; // Saltar el siguiente '/'
                columna++;
                continue;
            }
            
            // Manejar comentarios
            if (enComentarioLinea) {
                if (caracterActual == '\n') {
                    enComentarioLinea = false;
                    resultado.append(caracterActual);
                    fila++;
                    columna = 1;
                } else {
                    // Agregar al comentario actual
                    String lexemaActual = tokens.get(tokens.size() - 1).getLexeme();
                    tokens.remove(tokens.size() - 1);
                    Token tokenActualizado = new Token(TokenType.COMENTARIO_LINEA, lexemaActual + caracterActual, fila, columna - lexemaActual.length() + 1);
                    tokens.add(tokenActualizado);
                }
                columna++;
                continue;
            }
            
            if (enComentarioBloque) {
                if (caracterActual == '\n') {
                    fila++;
                    columna = 1;
                } else {
                    // Agregar al comentario de bloque actual
                    String lexemaActual = tokens.get(tokens.size() - 1).getLexeme();
                    tokens.remove(tokens.size() - 1);
                    Token tokenActualizado = new Token(TokenType.COMENTARIO_BLOQUE, lexemaActual + caracterActual, fila, columna - lexemaActual.length() + 1);
                    tokens.add(tokenActualizado);
                }
                columna++;
                continue;
            }
            
            // Carácter normal (no comentario)
            resultado.append(caracterActual);
            salida.append(caracterActual);
            
            if (caracterActual == '\n') {
                fila++;
                columna = 1;
            } else {
                columna++;
            }
        }
        
        return salida.toString();
    }
    
    private boolean esFinDeToken(int indiceActual, String entrada, Automata automata, char caracterActual) {
        if (indiceActual + 1 >= entrada.length()) {
            return true; // Fin del texto
        }
        
        char siguienteCaracter = entrada.charAt(indiceActual + 1);
        int estadoActual = automata.getCurrentState();
        
        // Si el siguiente carácter es espacio o separador, terminamos
        if (siguienteCaracter == ' ' || siguienteCaracter == '\t' || siguienteCaracter == '\n' || siguienteCaracter == '\r') {
            return true;
        }
        
        // Si el siguiente carácter es un operador, terminamos
        if ("()[]{};:,+-*/%=.<>".indexOf(siguienteCaracter) >= 0) {
            return true;
        }
        
        return false;
    }
    
    private void contarLexema(String lexema, TokenType tipo) {
        String clave = lexema + "|" + tipo;
        conteoLexemas.put(clave, conteoLexemas.getOrDefault(clave, 0) + 1);
    }
    
    // Métodos para obtener resultados
    public List<Token> getTokens() { return new ArrayList<>(tokens); }
    public List<String> getErrors() { return new ArrayList<>(errores); }
    public List<String> getAnalysisLog() { return new ArrayList<>(logAnalisis); }
    
    public String getResults() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== REPORTE DE ANÁLISIS LÉXICO ===\n\n");
        
        // Tokens encontrados
        sb.append("TOKENS RECONOCIDOS:\n");
        sb.append("-------------------\n");
        for (Token token : tokens) {
            if (token.getType() != TokenType.ERROR) {
                sb.append(token.toReportString()).append("\n");
            }
        }
        
        // Errores con sugerencias
        if (!errores.isEmpty()) {
            sb.append("\n=== REPORTE DE ERRORES CON SUGERENCIAS ===\n");
            sb.append("══════════════════════════════════════════════\n");
            
            int contadorError = 1;
            for (String error : errores) {
                sb.append("ERROR ").append(contadorError).append(":\n");
                sb.append(error).append("\n");
                sb.append("──────────────────────────────────────────\n");
                contadorError++;
            }
            
            sb.append("Total de errores: ").append(errores.size()).append("\n");
            
            // Estadísticas de sugerencias
            if (!sugerenciasAplicadas.isEmpty()) {
                sb.append("\nESTADÍSTICAS DE SUGERENCIAS:\n");
                sb.append("────────────────────────────\n");
                for (Map.Entry<String, Integer> entrada : sugerenciasAplicadas.entrySet()) {
                    String tipo = entrada.getKey();
                    int cantidad = entrada.getValue();
                    sb.append(String.format("- %s: %d veces\n", tipo, cantidad));
                }
            }
        }
        
        // Conteo de lexemas
        sb.append("\nRECUENTO DE LEXEMAS:\n");
        sb.append("-------------------\n");
        for (Map.Entry<String, Integer> entrada : conteoLexemas.entrySet()) {
            String[] partes = entrada.getKey().split("\\|");
            String lexema = partes[0];
            TokenType tipo = TokenType.valueOf(partes[1]);
            sb.append(String.format("%-20s %-15s Cantidad: %d\n", 
                "'" + lexema + "'", tipo, entrada.getValue()));
        }
        
        // Log del análisis
        sb.append("\nLOG DEL ANÁLISIS:\n");
        sb.append("----------------\n");
        for (String log : logAnalisis) {
            sb.append(log).append("\n");
        }
        
        return sb.toString();
    }
    
    public Map<String, Integer> getLexemeCount() {
        return new HashMap<>(conteoLexemas);
    }
    
    public Map<String, Integer> getSugerenciasAplicadas() {
        return new HashMap<>(sugerenciasAplicadas);
    }
}
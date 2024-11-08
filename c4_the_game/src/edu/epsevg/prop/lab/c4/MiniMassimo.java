package edu.epsevg.prop.lab.c4;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

/**
 * MiniMassimo: Jugador inteligente para Conecta 4 utilizando Minimax con poda alfa-beta y heurística mejorada.
 * @author 
 */
public class MiniMassimo implements Jugador, IAuto {

    private String nom;
    private int profunditatMaxima;

    public MiniMassimo(int d) {
        this.profunditatMaxima = d;
        this.nom = "MiniMassimo";
        this.profunditatMaxima = 8; // Aumentamos la profundidad máxima
    }

    @Override
    public int moviment(Tauler t, int color) {
        int millorMoviment = -1;
        int millorValor = Integer.MIN_VALUE;

        List<Integer> moviments = getMovimientosValidos(t);
        ordenarMovimientosCentrales(moviments, t);

        for (int col : moviments) {
            Tauler nouTauler = new Tauler(t);
            nouTauler.afegeix(col, color);
            int valorMoviment = minimax(nouTauler, profunditatMaxima - 1, false, color, Integer.MIN_VALUE, Integer.MAX_VALUE);

            if (valorMoviment > millorValor) {
                millorValor = valorMoviment;
                millorMoviment = col;
            }
        }

        return millorMoviment;
    }

    private int minimax(Tauler tauler, int profunditat, boolean maximitzant, int color, int alpha, int beta) {
        int resultat = evaluarTauler(tauler, color);

        if (profunditat == 0 || Math.abs(resultat) >= 1000000 || !tauler.espotmoure()) {
            return resultat;
        }

        if (maximitzant) {
            int maxValor = Integer.MIN_VALUE;
            List<Integer> moviments = getMovimientosValidos(tauler);
            ordenarMovimientosCentrales(moviments, tauler);

            for (int col : moviments) {
                Tauler nouTauler = new Tauler(tauler);
                nouTauler.afegeix(col, color);
                int valor = minimax(nouTauler, profunditat - 1, false, color, alpha, beta);
                maxValor = Math.max(maxValor, valor);
                alpha = Math.max(alpha, valor);
                if (beta <= alpha) {
                    break; // Poda beta
                }
            }
            return maxValor;
        } else {
            int minValor = Integer.MAX_VALUE;
            int oponentColor = -color;
            List<Integer> moviments = getMovimientosValidos(tauler);
            ordenarMovimientosCentrales(moviments, tauler);

            for (int col : moviments) {
                Tauler nouTauler = new Tauler(tauler);
                nouTauler.afegeix(col, oponentColor);
                int valor = minimax(nouTauler, profunditat - 1, true, color, alpha, beta);
                minValor = Math.min(minValor, valor);
                beta = Math.min(beta, valor);
                if (beta <= alpha) {
                    break; // Poda alfa
                }
            }
            return minValor;
        }
    }

    private int evaluarTauler(Tauler tauler, int color) {
        int puntuacion = 0;

        // Verificar si el jugador actual ha ganado
        if (haGanado(tauler, color)) {
            return 1000000;
        }

        // Verificar si el oponente ha ganado
        if (haGanado(tauler, -color)) {
            return -1000000;
        }

        // Heurística avanzada
        puntuacion += evaluarPosicion(tauler, color);
        
        
        return puntuacion;
    }

    private int evaluarPosicion(Tauler tauler, int color) {
        int puntuacion = 0;

        int centerColumn = tauler.getMida() / 2;
        // Priorizar control del centro
        for (int fila = 0; fila < tauler.getMida(); fila++) {
            if (tauler.getColor(fila, centerColumn) == color) {
                puntuacion += 6;
            }
        }

        // Evaluar todas las ventanas posibles de 4 fichas
        for (int fila = 0; fila < tauler.getMida(); fila++) {
            for (int col = 0; col < tauler.getMida() - 3; col++) {
                // Horizontal
                int[] ventana = new int[4];
                for (int i = 0; i < 4; i++) {
                    ventana[i] = tauler.getColor(fila, col + i);
                }
                puntuacion += evaluarVentana(ventana, color);
            }
        }

        for (int fila = 0; fila < tauler.getMida() - 3; fila++) {
            for (int col = 0; col < tauler.getMida(); col++) {
                // Vertical
                int[] ventana = new int[4];
                for (int i = 0; i < 4; i++) {
                    ventana[i] = tauler.getColor(fila + i, col);
                }
                puntuacion += evaluarVentana(ventana, color);
            }
        }

        for (int fila = 0; fila < tauler.getMida() - 3; fila++) {
            for (int col = 0; col < tauler.getMida() - 3; col++) {
                // Diagonal positiva
                int[] ventana = new int[4];
                for (int i = 0; i < 4; i++) {
                    ventana[i] = tauler.getColor(fila + i, col + i);
                }
                puntuacion += evaluarVentana(ventana, color);
            }
        }

        for (int fila = 3; fila < tauler.getMida(); fila++) {
            for (int col = 0; col < tauler.getMida() - 3; col++) {
                // Diagonal negativa
                int[] ventana = new int[4];
                for (int i = 0; i < 4; i++) {
                    ventana[i] = tauler.getColor(fila - i, col + i);
                }
                puntuacion += evaluarVentana(ventana, color);
            }
        }

        return puntuacion;
    }

    private int evaluarVentana(int[] ventana, int color) {
        int puntuacion = 0;
        int oponenteColor = -color;
        int conteoColor = 0;
        int conteoVacio = 0;
        int conteoOponente = 0;

        for (int celda : ventana) {
            if (celda == color) {
                conteoColor++;
            } else if (celda == oponenteColor) {
                conteoOponente++;
            } else {
                conteoVacio++;
            }
        }

        if (conteoColor == 4) {
            puntuacion += 100000;
        } else if (conteoColor == 3 && conteoVacio == 1) {
            puntuacion += 100;
        } else if (conteoColor == 2 && conteoVacio == 2) {
            puntuacion += 10;
        }

        if (conteoOponente == 3 && conteoVacio == 1) {
            puntuacion -= 80;
        }

        return puntuacion;
    }

    private boolean haGanado(Tauler tauler, int color) {
        // Verificar todas las posiciones del tablero para detectar una victoria
        for (int fila = 0; fila < tauler.getMida(); fila++) {
            for (int col = 0; col < tauler.getMida(); col++) {
                if (tauler.getColor(fila, col) == color) {
                    if (verificarDireccion(tauler, fila, col, 1, 0, color)) return true; // Horizontal
                    if (verificarDireccion(tauler, fila, col, 0, 1, color)) return true; // Vertical
                    if (verificarDireccion(tauler, fila, col, 1, 1, color)) return true; // Diagonal \
                    if (verificarDireccion(tauler, fila, col, 1, -1, color)) return true; // Diagonal /
                }
            }
        }
        return false;
    }

    private boolean verificarDireccion(Tauler tauler, int fila, int col, int dirX, int dirY, int color) {
        int contador = 0;
        for (int i = 0; i < 4; i++) {
            int x = fila + i * dirX;
            int y = col + i * dirY;
            if (x >= 0 && x < tauler.getMida() && y >= 0 && y < tauler.getMida() && tauler.getColor(x, y) == color) {
                contador++;
            } else {
                break;
            }
        }
        return contador == 4;
    }

    private List<Integer> getMovimientosValidos(Tauler tauler) {
        List<Integer> movimientos = new ArrayList<>();
        for (int col = 0; col < tauler.getMida(); col++) {
            if (tauler.movpossible(col)) {
                movimientos.add(col);
            }
        }
        return movimientos;
    }

    private void ordenarMovimientosCentrales(List<Integer> movimientos, Tauler tauler) {
        int center = tauler.getMida() / 2;
        Collections.sort(movimientos, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return Integer.compare(Math.abs(center - a), Math.abs(center - b));
            }
        });
    }

    @Override
    public String nom() {
        return this.nom;
    }
}


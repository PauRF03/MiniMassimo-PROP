package edu.epsevg.prop.lab.c4;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

/**
 * MiniMassimo: Jugador de Connecta 4 utilitzant Minimax amb poda alfa-beta.
 * @author Pau Ramos
 * @author Ilarion Tsekot
 */
public class MiniMassimo implements Jugador, IAuto {

    private String nom;
    private int profunditatMaxima;
    private int nodesExplorats;

    /**
     * Constructora
     * @param d correspon a la profunditat màxima a la que volem que arribi el jugador
     */
    public MiniMassimo(int d) {
        this.nodesExplorats = 0;
        this.profunditatMaxima = d;
        this.nom = "MiniMassimo";
    }

    /**
     * 
     * @param t
     * @param color
     * @return 
     */
    @Override
    public int moviment(Tauler t, int color) {
        int millorMoviment = -1; //No hi ha millor moviment inicialment
        int millorValor = Integer.MIN_VALUE;
        List<Integer> moviments = getMovimentsValids(t); //Obtenir tots els moviments possibles amb el tauler actual
        ordenarMoviments(moviments, t);
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

    /**
     * 
     * @param tauler
     * @param profunditat
     * @param maximitzant
     * @param color
     * @param alpha
     * @param beta
     * @return 
     */
    private int minimax(Tauler tauler, int profunditat, boolean maximitzant, int color, int alpha, int beta) {
        int resultat = avaluarTauler(tauler, color);

        if (profunditat == 0 || Math.abs(resultat) >= 1000000 || !tauler.espotmoure()) {
            return resultat;
        }

        if (maximitzant) {
            int maxValor = Integer.MIN_VALUE;
            List<Integer> moviments = getMovimentsValids(tauler);
            ordenarMoviments(moviments, tauler);
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
            List<Integer> moviments = getMovimentsValids(tauler);
            ordenarMoviments(moviments, tauler);

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

    /**
     * 
     * @param tauler tauler a analitzar
     * @param color el color del nostre jugador
     * @return heurística del tauler analitzat
     */
    private int avaluarTauler(Tauler tauler, int color) {
      
        //Verificar si hem guanyat
        if (haGuanyat(tauler, color)) return 1000000; //Si guanyem l'heurística és molt bona

        //Verificar si l'oponent ha guanyat
        if (haGuanyat(tauler, -color)) return -1000000; //Si perdem, l'heurística és molt dolenta
        
        return avaluarPosicio(tauler, color);
    }

    /**
     * 
     * @param tauler
     * @param color
     * @return 
     */
    private int avaluarPosicio(Tauler tauler, int color) {
        int puntuacio = 0;

        int centerColumn = tauler.getMida() / 2;
        // Priorizar control del centro
        for (int fila = 0; fila < tauler.getMida(); fila++) {
            if (tauler.getColor(fila, centerColumn) == color) {
                puntuacio += 6;
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
                puntuacio += evaluarFinestra(ventana, color);
            }
        }

        for (int fila = 0; fila < tauler.getMida() - 3; fila++) {
            for (int col = 0; col < tauler.getMida(); col++) {
                // Vertical
                int[] ventana = new int[4];
                for (int i = 0; i < 4; i++) {
                    ventana[i] = tauler.getColor(fila + i, col);
                }
                puntuacio += evaluarFinestra(ventana, color);
            }
        }

        for (int fila = 0; fila < tauler.getMida() - 3; fila++) {
            for (int col = 0; col < tauler.getMida() - 3; col++) {
                // Diagonal positiva
                int[] ventana = new int[4];
                for (int i = 0; i < 4; i++) {
                    ventana[i] = tauler.getColor(fila + i, col + i);
                }
                puntuacio += evaluarFinestra(ventana, color);
            }
        }

        for (int fila = 3; fila < tauler.getMida(); fila++) {
            for (int col = 0; col < tauler.getMida() - 3; col++) {
                // Diagonal negativa
                int[] ventana = new int[4];
                for (int i = 0; i < 4; i++) {
                    ventana[i] = tauler.getColor(fila - i, col + i);
                }
                puntuacio += evaluarFinestra(ventana, color);
            }
        }

        return puntuacio;
    }

    /**
     * 
     * @param finestra
     * @param color
     * @return 
     */
    private int evaluarFinestra(int[] finestra, int color) {
        int puntuacio = 0;
        int oponentColor = -color;
        int conteigColor = 0;
        int conteigBuit = 0;
        int conteigOponent = 0;
        for (int casella : finestra) {
            if (casella == color) {
                conteigColor++;
            } else if (casella == oponentColor) {
                conteigOponent++;
            } else {
                conteigBuit++;
            }
        }
        if (conteigColor == 4) {
            puntuacio += 100000;
        } else if (conteigColor == 3 && conteigBuit == 1) {
            puntuacio += 100;
        } else if (conteigColor == 2 && conteigBuit == 2) {
            puntuacio += 10;
        }

        if (conteigOponent == 3 && conteigBuit == 1) {
            puntuacio -= 80;
        }

        return puntuacio;
    }

    /**
     * Comprova si el color indicat guanya al tauler a analitzar
     * @param tauler tauler a analitzar
     * @param color color del jugador
     * @return true si el color indicat ha guanyat, false si no ho ha fet
     */
    private boolean haGuanyat(Tauler tauler, int color) {
        // Verificar totes les posicions del tauler per detectar una victoria
        for (int fila = 0; fila < tauler.getMida(); fila++) {
            for (int col = 0; col < tauler.getMida(); col++) {
                if (tauler.getColor(fila, col) == color) {
                    if (verificarDireccio(tauler, fila, col, 1, 0, color)) return true; // Horitzontal -
                    if (verificarDireccio(tauler, fila, col, 0, 1, color)) return true; // Vertical |
                    if (verificarDireccio(tauler, fila, col, 1, 1, color)) return true; // Diagonal \
                    if (verificarDireccio(tauler, fila, col, 1, -1, color)) return true; // Diagonal /
                }
            }
        }
        return false;
    }

    /**
     * Comprova si s'han connectat 4 peces en la direcció especificada
     * @param tauler tauler on es busquen les 4 peces connectades
     * @param fila
     * @param col
     * @param dirX
     * @param dirY
     * @param color color de les peces que es busquen si estan connectades
     * @return true si hi han quatre peces connectades, false si no
     */
    private boolean verificarDireccio(Tauler tauler, int fila, int col, int dirX, int dirY, int color) {
        int comptador = 0;
        for (int i = 0; i < 4; i++) {
            int x = fila + i * dirX;
            int y = col + i * dirY;
            if (x >= 0 && x < tauler.getMida() && y >= 0 && y < tauler.getMida() && tauler.getColor(x, y) == color) {
                comptador++;
            } else {
                break;
            }
        }
        return comptador == 4;
    }

    /**
     * Comprova a quines columnes es pot posar una fitxa i retorna els seus indexs
     * @param tauler tauler des del que es vol fer el moviment
     * @return una llista amb l'índex de cada columna on es pot llançar una fitxa
     */
    private List<Integer> getMovimentsValids(Tauler tauler) {
        List<Integer> moviments = new ArrayList<>();
        //Per cada columna, si es pot fer un moviment, s'afegeix a la llista
        for (int col = 0; col < tauler.getMida(); col++) {
            if (tauler.movpossible(col))  moviments.add(col);
        }
        return moviments;
    }

    /**
     * Ordena una llista de index de columnes de forma decreixent segons la proximitat amb la columna central del tauler 
     * @param moviments llista a ordenar amb els indexs de les columnes on es pot col·locar una fitxa
     * @param tauler tauler actual
     */
    private void ordenarMoviments(List<Integer> moviments, Tauler tauler) {
        int center = tauler.getMida() / 2;
        Collections.sort(moviments, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return Integer.compare(Math.abs(center - a), Math.abs(center - b));
            }
        });
    }
    
    /**
     * Getter del nom del jugador
     * @return nom del jugador
     */
    @Override
    public String nom() {
        return this.nom;
    }
}


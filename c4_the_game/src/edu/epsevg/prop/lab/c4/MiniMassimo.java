package edu.epsevg.prop.lab.c4;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

/**
 * MiniMassimo: Jugador de Connecta 4 utilitzant Minimax amb poda alfa-beta.
 *
 * @author Pau Ramos
 * @author Ilarion Tsekot
 */
public class MiniMassimo implements Jugador, IAuto {

    private final String nom;
    private final boolean poda, ordenacio;
    private final int profunditatMaxima;
    private int nodesTotalsExplorats, nodesExploratsMoviment;

    /**
     * Constructora
     *
     * @param d correspon a la profunditat màxima a la que volem que arribi el jugador
     * @param poda true si volem que el minimax utilitzi poda, false si no
     * @param ordenacio true si volem ordenar les columnes per realitzar moviments, false si no
     */
    public MiniMassimo(int d, boolean poda, boolean ordenacio) {
        this.nodesTotalsExplorats = 0;
        this.nodesExploratsMoviment = 0;
        this.profunditatMaxima = d;
        this.nom = "MiniMassimo";
        this.poda = poda;
        this.ordenacio = ordenacio;
    }

    /**
     * Funcio que calcula quin moviment s'ha de realitzar i envia la columna a
     * la que s'ha de col·locar una fitxa
     *
     * @param t tauler sobre el que es vol realitzar un moviment
     * @param color color del jugador
     * @return la columna corresponent al millor moviment realitzable després d'executar l'algorisme minimax
     */
    @Override
    public int moviment(Tauler t, int color) {
        int millorMoviment = -1; //No hi ha millor moviment inicialment
        int millorValor = Integer.MIN_VALUE;
        List<Integer> moviments = getMovimentsValids(t); //Obtenir tots els moviments possibles amb el tauler actual
        if(ordenacio) ordenarMoviments(moviments, t); //Ordenar els indexs de les columnes per afavorir la poda alfa-beta
        for (int col : moviments) {
            Tauler nouTauler = new Tauler(t);
            nouTauler.afegeix(col, color); //Per cada moviment possible, crear una copia del tauler i afegir-li la peça
            int valorMoviment = minimax(nouTauler, profunditatMaxima - 1, false, color, Integer.MIN_VALUE, Integer.MAX_VALUE); //avaluar el nou tauler per obtenir el valro heurístic del moviment
            if (valorMoviment > millorValor) { //Si s'obté un millor valor heurístic pel nou tauler, actualitzar les variables del valor i la columna 
                millorValor = valorMoviment;
                millorMoviment = col;
            }
        }
        nodesTotalsExplorats += nodesExploratsMoviment;
        System.out.println("Nodes explorats per fer el moviment: " + nodesExploratsMoviment + "; Total = " + nodesTotalsExplorats);
        nodesExploratsMoviment = 0;
        return millorMoviment;
    }

    /**
     * Implementa l'algoritme Minimax amb poda alfa-beta.
     *
     * @param tauler L'estat actual del joc.
     * @param profunditat El nivell del moviment.
     * @param maximitzant True si estem maximitzant (serà un torn del nostre jugador), false si estem minimitzant
     * @param color El color del nostre jugador
     * @param alpha El valor alfa per a la poda (millor opció del maximitzador).
     * @param beta El valor beta per a la poda (millor opció del minimitzador).
     * @return el millor valor heurístic trobat per al moviment actual.
     */
    private int minimax(Tauler tauler, int profunditat, boolean maximitzant, int color, int alpha, int beta) {
        int resultat = avaluarTauler(tauler, color); //Obtenir valor heurístic pel tauler

        this.nodesExploratsMoviment++; // Incrementa el comptador de nodes explorats

        //Si s'ha arribat a la profunditat màxima, s'ha guanyat o estan totes les columnes plenes retorna el resultat
        if (profunditat == 0 || Math.abs(resultat) >= 1000000 || !tauler.espotmoure()) {
            return resultat;
        }

        // Si és el torn del maximitzador (el nostre jugador):
        if (maximitzant) {
            int maxValor = Integer.MIN_VALUE; // Inicialitza el valor màxim a un valor molt baix.
            List<Integer> moviments = getMovimentsValids(tauler); // Obté les columnes disponibles per moure.
            if(ordenacio) ordenarMoviments(moviments, tauler);
            // Prova cada moviment disponible.
            for (int col : moviments) {
                Tauler nouTauler = new Tauler(tauler); // Crea una còpia del tauler per simular el moviment.
                nouTauler.afegeix(col, color); // Afegeix una peça del jugador actual en la columna seleccionada.
                // Avalua el moviment recursivament, passant al torn del minimitzador.
                int valor = minimax(nouTauler, profunditat - 1, false, color, alpha, beta);
                maxValor = Math.max(maxValor, valor); // Actualitza el valor màxim trobat fins ara. // Actualitza el límit alfa (millor opció coneguda per al maximitzador).
                alpha = Math.max(alpha, valor);
                // Poda beta: si el valor actual és millor que el límit beta, s'atura l'exploració.
                if (poda && beta <= alpha) {
                    break; // Poda beta
                }
            }
            return maxValor; // Retorna el millor valor trobat per al maximitzador.
        } else { // Si és el torn de l'oponent:
            int minValor = Integer.MAX_VALUE; // Inicialitza el valor mínim a un valor molt alt.
            int oponentColor = -color; // Color de l'oponent.
            List<Integer> moviments = getMovimentsValids(tauler); // Obté els moviments disponibles
            if(ordenacio)ordenarMoviments(moviments, tauler);

            // Prova cada moviment disponible.
            for (int col : moviments) {
                // Crea una còpia del tauler per simular el moviment de l'oponent.
                Tauler nouTauler = new Tauler(tauler);
                nouTauler.afegeix(col, oponentColor); // Afegeix una peça de l'oponent.
                // Avalua el moviment recursivament, passant al torn del maximitzador.
                int valor = minimax(nouTauler, profunditat - 1, true, color, alpha, beta);
                // Actualitza el valor mínim trobat fins ara.
                minValor = Math.min(minValor, valor);
                // Actualitza el límit beta (millor opció coneguda per al minimitzador).
                beta = Math.min(beta, valor);
                // Poda alfa: si el valor actual és pitjor que el límit alfa, s'atura l'exploració.
                if (poda && beta <= alpha) {
                    break; // Poda alfa
                }
            }
            return minValor; // Retorna el millor valor trobat per al minimitzador.
        }
    }

    /**
     * Avalua un tauler i retorna un valor heurístic per determinar com de bo és
     * l'estat actual del joc.
     *
     * @param tauler tauler a analitzar
     * @param color el color del nostre jugador
     * @return heurística del tauler analitzat
     */
    private int avaluarTauler(Tauler tauler, int color) {

        //Verificar si hem guanyat
        if (haGuanyat(tauler, color)) {
            return 1000000; //Si guanyem l'heurística és molt bona
        }
        //Verificar si l'oponent ha guanyat
        if (haGuanyat(tauler, -color)) {
            return -1000000; //Si perdem, l'heurística és molt dolenta
        }
        //Calcular heurística segons l'estat del tauler
        return avaluarPosicio(tauler, color);
    }

    /**
     * Avalua un tauler i retorna un valor heurístic tenint en compte totes 
     * les possibles finestres de 4 fitxes.
     * 
     * @param tauler tauler a analitzar
     * @param color el color del nostre jugador
     * @return la suma de puntuacions de totes les finestres possibles dins del tauler
     */
    private int avaluarPosicio(Tauler tauler, int color) {
        int puntuacio = 0;
        int centre = tauler.getMida() / 2;

        // Prioritzar el control de la columna central.
        for (int fila = 0; fila < tauler.getMida(); fila++) {
            // Suma puntuació si una cel·la de la columna central conté el color del jugador.
            if (tauler.getColor(fila, centre) == color) puntuacio += 6;
        }

        // Definim els desplaçaments per a cada direcció: horitzontal, vertical, diagonal ascendent (/) i diagonal descendent (\).
        int[][] direccions = {
            {0, 1}, // Horitzontal (dreta)
            {1, 0}, // Vertical (avall)
            {1, 1}, // Diagonal positiva (\)
            {-1, 1} // Diagonal negativa (/)
        };

        for (int[] direccio : direccions) {
            int dFila = direccio[0];
            int dCol = direccio[1];
            for (int fila = 0; fila < tauler.getMida(); fila++) {
                for (int col = 0; col < tauler.getMida(); col++) {
                    // Comprovem si és possible formar una finestra de 4 a partir d'aquest punt.
                    if (fila + 3 * dFila >= 0 && fila + 3 * dFila < tauler.getMida() && col + 3 * dCol >= 0 && col + 3 * dCol < tauler.getMida()) {
                        int[] finestra = new int[4];
                        for (int i = 0; i < 4; i++) finestra[i] = tauler.getColor(fila + i * dFila, col + i * dCol);
                        puntuacio += avaluarFinestra(finestra, color); // Avalua la finestra i suma la puntuació.
                    }
                }
            }
        }
        return puntuacio;
    }

    /**
     * Avalua una finestra específica de 4 cel·les i calcula una puntuació
     * basada en el contingut d'aquestes cel·les per al jugador i l'oponent.
     *
     * @param finestra array de 4 enters que representen les cel·les a
     * analitzar.
     * @param color el color del nostre jugador.
     * @return puntuació calculada per a aquesta finestra.
     */
    private int avaluarFinestra(int[] finestra, int color) {
        int puntuacio = 0;
        int oponentColor = -color; // Especifica el color de l'oponent.
        int conteigColor = 0; // Comptador de cel·les ocupades pel jugador.
        int conteigBuit = 0; // Comptador de cel·les buides.
        int conteigOponent = 0; // Comptador de cel·les ocupades per l'oponent.

        // Itera les caselles de la finestra i compta els tipus de valors.
        for (int casella : finestra) {
            if (casella == color) {
                conteigColor++;
            } else if (casella == oponentColor) {
                conteigOponent++;
            } else {
                conteigBuit++;
            }
        }

        // Si totes les cel·les són del jugador, atorga la puntuació per guanyar.
        if (conteigColor == 4) {
            puntuacio += 1000000;
            // Si el jugador té 3 cel·les i una buida, atorga una puntuació alta.
        } else if (conteigColor == 3 && conteigBuit == 1) {
            puntuacio += 100;
            // Si el jugador té 2 cel·les i dues buides, atorga una puntuació positiva però menor.
        } else if (conteigColor == 2 && conteigBuit == 2) {
            puntuacio += 10;
        }

        // Disminueix la puntuació si l'oponent té 3 cel·les i una buida.
        if (conteigOponent == 3 && conteigBuit == 1) {
            puntuacio -= 80;
        }

        return puntuacio; // Retorna la puntuació final de la finestra.
    }

    /**
     * Comprova si el color indicat guanya al tauler a analitzar
     *
     * @param tauler tauler a analitzar
     * @param color color del jugador
     * @return true si el color indicat ha guanyat, false si no ho ha fet
     */
    private boolean haGuanyat(Tauler tauler, int color) {
        // Verificar totes les posicions del tauler per detectar una victoria
        for (int fila = 0; fila < tauler.getMida(); fila++) {
            for (int col = 0; col < tauler.getMida(); col++) {
                if (tauler.getColor(fila, col) == color) {
                    if (verificarDireccio(tauler, fila, col, 1, 0, color)) {
                        return true; // Horitzontal -
                    }
                    if (verificarDireccio(tauler, fila, col, 0, 1, color)) {
                        return true; // Vertical |
                    }
                    if (verificarDireccio(tauler, fila, col, 1, 1, color)) {
                        return true; // Diagonal \
                    }
                    if (verificarDireccio(tauler, fila, col, 1, -1, color)) {
                        return true; // Diagonal /
                    }
                }
            }
        }
        return false;
    }

    /**
     * Comprova si s'han connectat 4 peces en la direcció especificada
     *
     * @param tauler tauler on es busquen les 4 peces connectades
     * @param fila indica la fila d'origen
     * @param col indica la columna d'origen
     * @param dirX indica si s'està mirant la direcció horitzontalment
     * @param dirY indica si s'esta mirant la direcció verticalment (i en quin sentit)
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
     * Comprova a quines columnes es pot posar una fitxa i retorna els seus
     * indexs
     *
     * @param tauler tauler des del que es vol fer el moviment
     * @return una llista amb l'índex de cada columna on es pot llançar una fitxa
     */
    private List<Integer> getMovimentsValids(Tauler tauler) {
        List<Integer> moviments = new ArrayList<>();
        //Per cada columna, si es pot fer un moviment, s'afegeix a la llista
        for (int col = 0; col < tauler.getMida(); col++) {
            if (tauler.movpossible(col)) {
                moviments.add(col);
            }
        }
        return moviments;
    }

    /**
     * Ordena una llista de index de columnes de forma decreixent segons la
     * proximitat a la columna central del tauler
     *
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
     *
     * @return nom del jugador
     */
    @Override
    public String nom() {
        return this.nom;
    }
}

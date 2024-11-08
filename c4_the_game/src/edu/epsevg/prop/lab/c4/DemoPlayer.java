/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.epsevg.prop.lab.c4;

/**
 *
 * @author paura
 */
public class DemoPlayer implements Jugador, IAuto{

    @Override
    public int moviment(Tauler t, int color) {
        for(int i=0; i<t.getMida(); i++){
            if(t.movpossible(i)) return i;
        }
        return 0;
    }

    @Override
    public String nom() {
        return "DEMOPLAYER";
    }
    
}

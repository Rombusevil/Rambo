package com.rombosaur.engine.screens;

import com.badlogic.gdx.utils.Array;

/**
 * Lazy Stack implementation
 * Created by rombus on 02/03/17.
 */
public class Stack<T> {
    private Array<T> elements; //TODO: hacerla portable y no usar Array de libgdx, manejar un array primitivo manualmente

    public Stack(){
        elements = new Array<T>();
    }

    public void push(T elemen){
        elements.add(elemen);
    }

    public T pop(){
        return elements.removeIndex(elements.size - 1);
    }

    public boolean isEmpty(){
        return (elements.size > 0)? false:true;
    }
}

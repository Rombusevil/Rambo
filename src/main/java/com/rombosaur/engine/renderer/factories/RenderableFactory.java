package com.rombosaur.engine.renderer.factories;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.rombosaur.engine.renderer.Renderable;

/**
 * Un factory que tiene un pool de Renderables.
 * Te devuelve el primer renderable disponible mediante el método get() y llama al método init(x,y) antes.
 *
 * Created by rombus on 29/04/17.
 */
public class RenderableFactory<T extends Renderable> implements Pool.Poolable{
    protected final Array<T> actives;
    protected final Pool<T> pool;

    public RenderableFactory(final Class type) {
        actives = new Array<T>();
        pool = new Pool<T>() {
            @Override
            protected T newObject() {
                try {
                    return (T)ClassReflection.newInstance(type);
                } catch (ReflectionException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    public T get(float x, float y){
        T obj = pool.obtain();
        obj.init(x, y);
        actives.add(obj);

        return obj;
    }

    @Override
    public void reset() {
        pool.freeAll(actives);
        actives.clear();
    }
}


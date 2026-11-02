package com.example.application.ai;

import com.vaadin.draiv.script.Script;
import com.vaadin.draiv.script.ScriptStore;
import com.vaadin.draiv.store.ItemNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The draiv scripts (macros) the AI saves with {@code save_script}. draiv leaves storage to the
 * application; a trade-show demo starts fresh on every restart, so memory is enough.
 */
class InMemoryScriptStore implements ScriptStore {

    private final Map<String, Script> scripts = new ConcurrentHashMap<>();
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    @Override
    public List<String> list() {
        return scripts.keySet().stream().sorted().toList();
    }

    @Override
    public Script load(String name) {
        var script = scripts.get(name);
        if (script == null) {
            throw new ItemNotFoundException("Script", name);
        }
        return script;
    }

    @Override
    public boolean exists(String name) {
        return scripts.containsKey(name);
    }

    @Override
    public void save(Script script) {
        scripts.put(script.name(), script);
        listeners.forEach(Runnable::run);
    }

    @Override
    public void delete(String name) {
        if (scripts.remove(name) != null) {
            listeners.forEach(Runnable::run);
        }
    }

    @Override
    public void addChangeListener(Runnable listener) {
        listeners.add(listener);
    }

    @Override
    public void removeChangeListener(Runnable listener) {
        listeners.remove(listener);
    }
}

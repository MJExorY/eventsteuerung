package org.simulation;

import States.IStates;
import States.RoamingState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Int2D;

import java.awt.*;

public class Agent implements Steppable {
    private IStates currentState = new RoamingState(); // Startzustand
    private Int2D targetPosition = null;
    private boolean isRoaming = false;
    private boolean isHungry = false;
    private boolean isWatchingMain = false;
    private boolean isWatchingSide = false;
    private boolean isInQueue = false;
    private boolean isPanicking = false;
    private boolean isWC = false;
    private Zone currentZone = null;
    private Zone.ZoneType lastVisitedZone = null;

    private Event event;

    public void setEvent(Event event) {
        this.event = event;
    }

    private Stoppable stopper;

    public void setStopper(Stoppable stopper) {
        this.stopper = stopper;
    }

    public Zone getCurrentZone() {
        return currentZone;
    }

    public void setCurrentZone(Zone zone) {
        this.currentZone = zone;
    }


    public void resetFlags() { // Setzt Zustand zurück - wichtig für State wechsel
        isWatchingMain = false;
        isWatchingSide = false;
        isInQueue = false;
        isPanicking = false;
        isHungry = false;
        isWC = false;
        currentZone = null;
    }


    // Getter & Setter
    public boolean isHungry() {
        return isHungry;
    }

    public boolean isWC() {
        return isWC;
    }

    public void setWC(boolean WC) {
        isWC = WC;
    }

    public boolean isRoaming() {
        return isRoaming;
    }

    public void setRoaming(boolean roaming) {
        isRoaming = roaming;
    }

    public void setHungry(boolean hungry) {
        this.isHungry = hungry;
    }

    public boolean isWatchingMain() {
        return isWatchingMain;
    }

    public void setWatchingMain(boolean watchingMain) {
        this.isWatchingMain = watchingMain;
    }

    public boolean isWatchingSide() {
        return isWatchingSide;
    }

    public void setWatchingSide(boolean watchingSide) {
        this.isWatchingSide = watchingSide;
    }

    public boolean isInQueue() {
        return isInQueue;
    }

    public void setInQueue(boolean inQueue) {
        this.isInQueue = inQueue;
    }

    public boolean isPanicking() {
        return isPanicking;
    }

    public void setPanicking(boolean panicking) {
        isPanicking = panicking;
    }

    public IStates getCurrentState() {
        return currentState;
    }

    public void setCurrentState(IStates state) {
        this.currentState = state;
    }

    public Int2D getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(Int2D position) {
        this.targetPosition = position;
    }

    public boolean hasTarget() {
        return targetPosition != null;
    }

    public void clearTarget() {
        targetPosition = null;
    }

    public Zone.ZoneType getLastVisitedZone() {
        return lastVisitedZone;
    }

    public void setLastVisitedZone(Zone.ZoneType type) {
        this.lastVisitedZone = type;
    }

    public Color getColor() {
        if (isPanicking) return Color.RED;
        if (isHungry) return Color.GREEN;
        if (isWatchingMain) return Color.BLUE;
        if (isWatchingSide) return Color.CYAN;
        if (isInQueue) return Color.MAGENTA;
        if (isWC) return Color.PINK;
        if (isRoaming) return Color.YELLOW;
        return Color.YELLOW; // Roaming
    }


    @Override
    public void step(SimState state) {
        Event sim = (Event) state;

        // Aktuellen Zustand ausführen
        if (currentState != null) {
            currentState = currentState.act(this, sim);
        }

        Int2D pos = sim.grid.getObjectLocation(this);
        sim.grid.setObjectLocation(this, pos);

        // Zufällige Bewegung (falls kein Ziel gesetzt)
        if (targetPosition == null) {
            int dx = sim.random.nextInt(3) - 1;
            int dy = sim.random.nextInt(3) - 1;
            int newX = Math.max(0, Math.min(sim.grid.getWidth() - 1, pos.x + dx));
            int newY = Math.max(0, Math.min(sim.grid.getHeight() - 1, pos.y + dy));
            sim.grid.setObjectLocation(this, new Int2D(newX, newY));
        }

        // Nur panische Agenten dürfen bei Emergency Exit entfernt werden
        Zone currentZone = sim.getZoneByPosition(pos);
        if (currentZone != null &&
                (currentZone.getType() == Zone.ZoneType.EXIT ||
                        (currentZone.getType() == Zone.ZoneType.EMERGENCY_EXIT && isPanicking))) {

            System.out.println("Agent hat " + currentZone.getType() + " erreicht und wird entfernt: " + pos);

            if (stopper != null) stopper.stop();
            sim.grid.remove(this);
            sim.agents.remove(this);
            return;
        }

        // Debug
        System.out.println("Agent @ " + pos
                + " | State: " + currentState.getClass().getSimpleName()
                + " | target: " + targetPosition);
    }

    private boolean alarmed = false;

    public boolean isAlarmed() {
        return alarmed;
    }

    public void setAlarmed(boolean alarmed) {
        this.alarmed = alarmed;
    }

    public boolean tryEnterZone(Zone targetZone) {
        if (!targetZone.isFull()) {
            if (currentZone != null) {
                currentZone.leave(this);
            }

            targetZone.enter(this);
            setCurrentZone(targetZone);
            setLastVisitedZone(targetZone.getType());
            clearTarget();
            setInQueue(false);
            return true;
        }
        return false;
    }
}

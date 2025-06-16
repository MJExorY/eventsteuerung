package States;

import org.simulation.Agent;
import org.simulation.Event;
import org.simulation.Zone;
import sim.util.Int2D;

public class WatchingSideActState implements IStates {

    private boolean initialized = false;
    private Int2D target;
    private int ticksInZone = 0;
    private final int WAITTIME = 40;
    private boolean hasEnteredZone = false;

    @Override
    public IStates act(Agent agent, Event event) {
        if (!initialized) {
            agent.resetFlags();
            agent.setWatchingSide(true);

            Zone sideAct = event.getZoneByType(Zone.ZoneType.ACT_SIDE);
            if (sideAct != null) {
                target = sideAct.getPosition();
                agent.setTargetPosition(target);
                initialized = true;
            } else {
                return new RoamingState(); // Fallback
            }
        }

        Int2D currentPos = event.grid.getObjectLocation(agent);

        // Wenn Ziel erreicht und noch nicht eingetreten
        if (!hasEnteredZone && currentPos.equals(target)) {
            Zone zone = event.getZoneByPosition(target);
            if (zone != null && agent.tryEnterZone(zone)) {
                hasEnteredZone = true;
            }
        }

        // Wenn in der Zone → Zeit zählen und leicht bewegen
        if (hasEnteredZone) {
            ticksInZone++;

            // Leichte zufällige Bewegung innerhalb der Zone
            int dx = event.random.nextInt(3) - 1;
            int dy = event.random.nextInt(3) - 1;
            int newX = Math.max(0, Math.min(event.grid.getWidth() - 1, currentPos.x + dx));
            int newY = Math.max(0, Math.min(event.grid.getHeight() - 1, currentPos.y + dy));
            event.grid.setObjectLocation(agent, new Int2D(newX, newY));

            // Nach WAITTIME wieder in Roaming zurück
            if (ticksInZone >= WAITTIME) {
                Zone zone = agent.getCurrentZone();
                if (zone != null) {
                    zone.leave(agent);
                    agent.setCurrentZone(null);
                }
                agent.setWatchingSide(false);
                return new RoamingState();
            }

            return this;
        }

        // Noch auf dem Weg zur Zone → weiter bewegen
        int dx = Integer.compare(target.x, currentPos.x);
        int dy = Integer.compare(target.y, currentPos.y);
        int newX = Math.max(0, Math.min(event.grid.getWidth() - 1, currentPos.x + dx));
        int newY = Math.max(0, Math.min(event.grid.getHeight() - 1, currentPos.y + dy));
        event.grid.setObjectLocation(agent, new Int2D(newX, newY));

        return this;
    }
}

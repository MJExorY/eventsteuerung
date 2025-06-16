package States;

import org.simulation.Agent;
import org.simulation.Event;
import sim.util.Int2D;

public class RoamingState implements IStates {

    @Override
    public IStates act(Agent agent, Event event) {
        agent.setRoaming(true);
        agent.resetFlags();
        Int2D pos = event.grid.getObjectLocation(agent);

        // Bewegung
        int dx = event.random.nextInt(3) - 1;
        int dy = event.random.nextInt(3) - 1;
        int newX = Math.max(0, Math.min(event.grid.getWidth() - 1, pos.x + dx));
        int newY = Math.max(0, Math.min(event.grid.getHeight() - 1, pos.y + dy));
        event.grid.setObjectLocation(agent, new Int2D(newX, newY));


        // Zustandswechsel (zuerst prüfen!)
        if (event.random.nextDouble() < 0.005) {
            agent.setHungry(true);
            return new HungryThirstyState();
        }

        if (event.random.nextDouble() < 0.003) {
            agent.setWC(true);
            return new WCState();
        }

        if (event.random.nextDouble() < 0.015) {
            agent.setWatchingMain(true);
            return new WatchingMainActState();
        }

        if (event.random.nextDouble() < 0.011) {
            agent.setWatchingSide(true);
            return new WatchingSideActState();
        }

        /* if (event.random.nextDouble() < 0.003) {
            agent.setPanicking(true);
            return new PanicRunState();
        } */

        /* if (event.random.nextDouble() < 0.005) {
            agent.setInQueue(true);
            return new QueueingState(agent, event.getZoneByType(Zone.ZoneType.EXIT));
        }
*/

        return this;
    }

}

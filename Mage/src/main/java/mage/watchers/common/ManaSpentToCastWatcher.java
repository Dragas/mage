package mage.watchers.common;

import mage.Mana;
import mage.constants.WatcherScope;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.game.stack.Spell;
import mage.watchers.Watcher;

/**
 * Watcher saves the mana that was spent to cast a spell
 *
 * @author LevelX2
 */
public class ManaSpentToCastWatcher extends Watcher {

    private Mana payment = null;

    public ManaSpentToCastWatcher() {
        super(WatcherScope.CARD);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        // There was a check for the from zone being the hand, but that should not matter
        if (event.getType() == GameEvent.EventType.SPELL_CAST) {
            Spell spell = (Spell) game.getObject(event.getTargetId());
            if (spell != null && this.getSourceId().equals(spell.getSourceId())) {
                payment = spell.getSpellAbility().getManaCostsToPay().getPayment();
            }
        }
        if (event.getType() == GameEvent.EventType.ZONE_CHANGE
                && this.getSourceId().equals(event.getSourceId())) {
            if (((ZoneChangeEvent) event).getFromZone() == Zone.BATTLEFIELD) {
                payment = null;
            }
        }
    }

    public Mana getAndResetLastPayment() {
        Mana returnPayment = null;
        if (payment != null) {
            returnPayment = payment.copy();
        }
        return returnPayment;

    }

    @Override
    public void reset() {
        super.reset();
        payment = null;
    }

}

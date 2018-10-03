package seedu.address.commons.events.ui;

import seedu.address.commons.events.BaseEvent;

/**
 * Indicates that a panel swap is required.
 */
public class SwapLeftPanelEvent extends BaseEvent {

    /**
     * Stores the two types of panels to swap to.
     */
    public enum PanelType {
        LIST, STATISTIC
    }

    private PanelType panelType;

    public SwapLeftPanelEvent(PanelType type) {
        panelType = type;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public PanelType getPanelType() {
        return panelType;
    }
}


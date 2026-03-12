/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.login.compat.nms.v1_17_r1_v1_20_r3;

import dev.helix.suleiman.login.compat.visual.AbstractVisualAdapter;

public final class Modern117To1203VisualAdapter extends AbstractVisualAdapter {
    @Override
    public String family() {
        return "v1_17_r1_v1_20_r3";
    }

    @Override
    protected boolean supportsTitles() {
        return true;
    }

    @Override
    protected boolean supportsBossBar() {
        return true;
    }

    @Override
    protected boolean supportsHologram() {
        return true;
    }
}

/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.login.compat.nms.v1_7_r4;

import dev.helix.suleiman.login.compat.visual.AbstractVisualAdapter;

public final class Legacy17VisualAdapter extends AbstractVisualAdapter {
    @Override
    public String family() {
        return "v1_7_r4";
    }

    @Override
    protected boolean supportsTitles() {
        return false;
    }

    @Override
    protected boolean supportsActionBar() {
        return false;
    }

    @Override
    protected boolean supportsBossBar() {
        return false;
    }

    @Override
    protected boolean supportsHologram() {
        return false;
    }
}

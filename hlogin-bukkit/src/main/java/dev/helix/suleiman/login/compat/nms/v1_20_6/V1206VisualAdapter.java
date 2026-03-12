/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.login.compat.nms.v1_20_6;

import dev.helix.suleiman.login.compat.visual.AbstractVisualAdapter;

public final class V1206VisualAdapter extends AbstractVisualAdapter {
    @Override
    public String family() {
        return "v1_20_6";
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

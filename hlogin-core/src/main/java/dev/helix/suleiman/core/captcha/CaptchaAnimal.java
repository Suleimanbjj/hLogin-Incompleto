/*
 * hLogin
 * Copyright (c) 2026 SuleimanDEV
 *
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited.
 *
 * Proprietary and confidential.
 */

package dev.helix.suleiman.core.captcha;

public enum CaptchaAnimal {
    PENGUIN("penguin"),
    FOX("fox"),
    ELEPHANT("elephant"),
    ZEBRA("zebra"),
    LION("lion");

    private final String id;

    CaptchaAnimal(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String translationKey() {
        return "captcha.animals." + id;
    }

    public static CaptchaAnimal fromId(String id) {
        if (id == null) {
            return null;
        }
        for (CaptchaAnimal animal : values()) {
            if (animal.id.equalsIgnoreCase(id)) {
                return animal;
            }
        }
        return null;
    }
}

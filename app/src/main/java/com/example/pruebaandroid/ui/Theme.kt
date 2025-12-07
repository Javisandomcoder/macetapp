package com.example.pruebaandroid.ui

enum class Theme {
    LIGHT,
    DARK,
    SYSTEM,
    OCEAN,
    SUNSET,
    FOREST,
    LAVENDER,
    MIDNIGHT,
    CORAL,
    ROYAL,
    MINT
}

// Extension property to get theme description for user feedback
val Theme.themeDescription: String
    get() = when (this) {
        Theme.LIGHT -> "Tema claro predeterminado"
        Theme.DARK -> "Tema oscuro predeterminado"
        Theme.SYSTEM -> "Sigue el tema del sistema"
        Theme.OCEAN -> "Colores azulados como el océano"
        Theme.SUNSET -> "Tonos cálidos de atardecer"
        Theme.FOREST -> "Verdes naturales del bosque"
        Theme.LAVENDER -> "Tonos suaves de lavanda"
        Theme.MIDNIGHT -> "Noche elegante con azul oscuro"
        Theme.CORAL -> "Naranja vibrante y turquesa"
        Theme.ROYAL -> "Azul y púrpura elegantes"
        Theme.MINT -> "Verdes frescos y naturales"
    }

// Extension property to get user-friendly display name
val Theme.displayName: String
    get() = when (this) {
        Theme.LIGHT -> "Claro"
        Theme.DARK -> "Oscuro"
        Theme.SYSTEM -> "Sistema"
        Theme.OCEAN -> "Océano"
        Theme.SUNSET -> "Atardecer"
        Theme.FOREST -> "Bosque"
        Theme.LAVENDER -> "Lavanda"
        Theme.MIDNIGHT -> "Medianoche"
        Theme.CORAL -> "Coral"
        Theme.ROYAL -> "Real"
        Theme.MINT -> "Menta"
    }
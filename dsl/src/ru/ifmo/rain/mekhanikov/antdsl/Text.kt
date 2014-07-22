package ru.ifmo.rain.mekhanikov.antdsl

trait DSLTextContainer: DSLTask

public fun DSLTextContainer.text(init: DSLTextContainer.() -> String) {
    nestedText = init()
}



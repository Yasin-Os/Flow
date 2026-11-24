package io.github.aedev.flow.data.contentfilter.family

/**
 * The family app's always-on switch, and the process-wide matchers it needs where dependency
 * injection cannot reach: [io.github.aedev.flow.data.recommendation.FlowNeuroEngine] and the
 * player metadata applier are singletons, so they read the matchers from here instead of a
 * constructor. [io.github.aedev.flow.FlowApplication] sets both providers at startup, from the
 * injected repositories; until then the channel matcher allows nothing (fail closed) and the
 * text matcher carries the built-in word lists.
 */
object FamilyMode {
    /** This fork is the family app: the whitelist and its filters are always on. */
    const val ENABLED: Boolean = true

    @Volatile
    var channelMatcher: () -> FamilyChannelMatcher = { FamilyChannelMatcher.EMPTY }

    @Volatile
    var textMatcher: () -> FamilyTextMatcher = { FamilyTextMatcher.DEFAULT }
}

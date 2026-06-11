package com.lifeup.app.ui.feedback

import java.time.LocalTime

/**
 * Time-of-day greeting utility.
 * Inspired by Duolingo's "¡Buenos días!" and Notion's personalized greetings.
 */
object Greeting {

    data class GreetingContext(
        val greeting: String,
        val emoji: String,
        val subtitle: String
    )

    fun forHour(hour: Int = LocalTime.now().hour): GreetingContext {
        return when (hour) {
            in 5..10 -> GreetingContext(
                greeting = "早安",
                emoji = "🌅",
                subtitle = "新的一天，从一个微小的开始"
            )
            in 11..13 -> GreetingContext(
                greeting = "午安",
                emoji = "☀️",
                subtitle = "专注当下，每分每秒都算数"
            )
            in 14..17 -> GreetingContext(
                greeting = "下午好",
                emoji = "🌤️",
                subtitle = "保持节奏，不要停下脚步"
            )
            in 18..21 -> GreetingContext(
                greeting = "晚上好",
                emoji = "🌆",
                subtitle = "复盘今日，迎接明日"
            )
            in 22..23, in 0..4 -> GreetingContext(
                greeting = "夜深了",
                emoji = "🌙",
                subtitle = "好好休息，技能不会跑掉"
            )
            else -> GreetingContext(
                greeting = "你好",
                emoji = "👋",
                subtitle = "今天想做点什么？"
            )
        }
    }
}

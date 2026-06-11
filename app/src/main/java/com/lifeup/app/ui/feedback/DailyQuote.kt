package com.lifeup.app.ui.feedback

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Curated daily inspirational quotes about growth, learning, and persistence.
 * Inspired by Duolingo's daily motivation cards and Headspace's daily inspiration.
 */
object DailyQuote {

    private val quotes = listOf(
        Quote(
            text = "We are what we repeatedly do. Excellence, then, is not an act, but a habit.",
            author = "Aristotle",
            translationZh = "我们是我们反复做的事情。因此，卓越不是一种行为，而是一种习惯。"
        ),
        Quote(
            text = "The secret of getting ahead is getting started.",
            author = "Mark Twain",
            translationZh = "取得成功的秘诀是开始行动。"
        ),
        Quote(
            text = "Discipline equals freedom.",
            author = "Jocko Willink",
            translationZh = "自律即自由。"
        ),
        Quote(
            text = "You don't have to be great to start, but you have to start to be great.",
            author = "Zig Ziglar",
            translationZh = "开始时不需要很厉害，开始之后才会变得厉害。"
        ),
        Quote(
            text = "Small daily improvements are the key to staggering long-term results.",
            author = "Robin Sharma",
            translationZh = "每天微小的进步，是长期惊人结果的关键。"
        ),
        Quote(
            text = "The only way to do great work is to love what you do.",
            author = "Steve Jobs",
            translationZh = "成就伟业的唯一途径是热爱你所做的事。"
        ),
        Quote(
            text = "Quality is not an act, it is a habit.",
            author = "Aristotle",
            translationZh = "优秀不是一种行为，而是一种习惯。"
        ),
        Quote(
            text = "Do the hard jobs first. The easy jobs will take care of themselves.",
            author = "Dale Carnegie",
            translationZh = "先做困难的事，简单的事自然会迎刃而解。"
        ),
        Quote(
            text = "Success is the sum of small efforts repeated day in and day out.",
            author = "Robert Collier",
            translationZh = "成功是日复一日小努力的累积。"
        ),
        Quote(
            text = "First we make our habits, then our habits make us.",
            author = "Charles C. Noble",
            translationZh = "先是我们塑造习惯，然后习惯塑造我们。"
        ),
        Quote(
            text = "Time is the most valuable thing a man can spend.",
            author = "Theophrastus",
            translationZh = "时间是一个人能花费的最有价值之物。"
        ),
        Quote(
            text = "Lost time is never found again.",
            author = "Benjamin Franklin",
            translationZh = "失去的时间再也找不回来。"
        ),
        Quote(
            text = "It does not matter how slowly you go as long as you do not stop.",
            author = "Confucius",
            translationZh = "前进的速度并不重要，只要你不停止。"
        ),
        Quote(
            text = "A year from now you may wish you had started today.",
            author = "Karen Lamb",
            translationZh = "一年后你可能会希望今天就开始了。"
        ),
        Quote(
            text = "The future depends on what you do today.",
            author = "Mahatma Gandhi",
            translationZh = "未来取决于你今天所做的事。"
        )
    )

    /**
     * Returns the quote for a given day index since epoch.
     * The same day always shows the same quote.
     */
    fun forDate(date: LocalDate = LocalDate.now()): Quote {
        val daysSinceEpoch = ChronoUnit.DAYS.between(LocalDate.of(2024, 1, 1), date)
        val index = ((daysSinceEpoch % quotes.size) + quotes.size).toInt() % quotes.size
        return quotes[index]
    }

    data class Quote(
        val text: String,
        val author: String,
        val translationZh: String
    )
}

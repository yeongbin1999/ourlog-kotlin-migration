"use client"

import * as React from "react"
import { ChevronLeft, ChevronRight } from "lucide-react"
import {
    format,
    addMonths,
    subMonths,
    startOfMonth,
    endOfMonth,
    startOfWeek,
    endOfWeek,
    addDays,
    isSameMonth,
    isSameDay,
    isToday,
} from "date-fns"
import { ko } from "date-fns/locale"

const cn = (...classes: (string | undefined | null | false)[]) => {
    return classes.filter(Boolean).join(" ")
}

export interface CalendarProps {
    mode?: "single"
    selected?: Date
    onSelect?: (date: Date | undefined) => void
    className?: string
    disabled?: (date: Date) => boolean
}

function Calendar({ className, selected, onSelect, disabled, ...props }: CalendarProps) {
    const [currentMonth, setCurrentMonth] = React.useState(selected || new Date())

    const monthStart = startOfMonth(currentMonth)
    const monthEnd = endOfMonth(monthStart)
    const startDate = startOfWeek(monthStart)
    const endDate = endOfWeek(monthEnd)

    const dateFormat = "d"
    const rows = []
    let days = []
    let day = startDate
    let formattedDate = ""

    while (day <= endDate) {
        for (let i = 0; i < 7; i++) {
            formattedDate = format(day, dateFormat)
            const cloneDay = day
            const isDisabled = disabled ? disabled(cloneDay) : false
            const isSelectedDay = selected && isSameDay(day, selected)
            const isTodayDay = isToday(day)
            const isCurrentMonth = isSameMonth(day, monthStart)

            days.push(
                <div
                    className={cn(
                        "h-9 w-9 p-0 font-normal cursor-pointer flex items-center justify-center text-sm rounded-md transition-colors",
                        !isCurrentMonth && "text-gray-400",
                        isCurrentMonth && "text-gray-900 hover:bg-gray-100",
                        isSelectedDay && "bg-blue-600 text-white hover:bg-blue-600",
                        isTodayDay && !isSelectedDay && "bg-gray-100 text-gray-900",
                        isDisabled && "text-gray-400 cursor-not-allowed",
                    )}
                    key={day.toString()}
                    onClick={() => {
                        if (!isDisabled && onSelect) {
                            onSelect(cloneDay)
                        }
                    }}
                >
                    <span>{formattedDate}</span>
                </div>,
            )
            day = addDays(day, 1)
        }
        rows.push(
            <div className="flex w-full mt-2" key={day.toString()}>
                {days}
            </div>,
        )
        days = []
    }

    const nextMonth = () => {
        setCurrentMonth(addMonths(currentMonth, 1))
    }

    const prevMonth = () => {
        setCurrentMonth(subMonths(currentMonth, 1))
    }

    return (
        <div className={cn("p-3", className)} {...props}>
            <div className="flex flex-col space-y-4">
                <div className="space-y-4">
                    <div className="flex justify-center pt-1 relative items-center">
                        <div className="text-sm font-medium">{format(currentMonth, "yyyy년 MMMM", { locale: ko })}</div>
                        <button
                            onClick={prevMonth}
                            className="absolute left-1 inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 border border-gray-300 bg-white hover:bg-gray-50 hover:text-gray-900 h-7 w-7 bg-transparent p-0 opacity-50 hover:opacity-100"
                        >
                            <ChevronLeft className="h-4 w-4" />
                        </button>
                        <button
                            onClick={nextMonth}
                            className="absolute right-1 inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 border border-gray-300 bg-white hover:bg-gray-50 hover:text-gray-900 h-7 w-7 bg-transparent p-0 opacity-50 hover:opacity-100"
                        >
                            <ChevronRight className="h-4 w-4" />
                        </button>
                    </div>
                    <div className="w-full border-collapse space-y-1">
                        <div className="flex">
                            {["일", "월", "화", "수", "목", "금", "토"].map((day) => (
                                <div key={day} className="text-gray-500 rounded-md w-9 font-normal text-sm text-center">
                                    {day}
                                </div>
                            ))}
                        </div>
                        {rows}
                    </div>
                </div>
            </div>
        </div>
    )
}

Calendar.displayName = "Calendar"

export { Calendar }

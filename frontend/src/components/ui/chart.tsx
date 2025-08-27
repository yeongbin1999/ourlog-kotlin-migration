"use client"

import * as React from "react"

const cn = (...classes: (string | undefined | null | false)[]) => {
    return classes.filter(Boolean).join(" ")
}

interface ChartContainerProps extends React.HTMLAttributes<HTMLDivElement> {
    children: React.ReactNode
}

const ChartContainer = React.forwardRef<HTMLDivElement, ChartContainerProps>(
    ({ className, children, ...props }, ref) => {
        return (
            <div ref={ref} className={cn("w-full", className)} {...props}>
                {children}
            </div>
        )
    },
)
ChartContainer.displayName = "ChartContainer"

interface ChartTooltipProps {
    active?: boolean;
    payload?: Array<{
        dataKey: string;
        value: number;
        color: string;
    }>;
    label?: string;
}

const ChartTooltip = ({ active, payload, label }: ChartTooltipProps) => {
    if (active && payload && payload.length) {
        return (
            <div className="rounded-lg border border-gray-200 bg-white p-2 shadow-sm">
                <div className="grid gap-2">
                    <div className="flex flex-col">
                        <span className="text-xs uppercase text-gray-500">{label}</span>
                    </div>
                    {payload.map((entry, index: number) => (
                        <div key={index} className="flex items-center gap-2">
                            <div className="h-2.5 w-2.5 shrink-0 rounded-sm" style={{ backgroundColor: entry.color }} />
                            <span className="text-sm text-gray-500">{entry.dataKey}:</span>
                            <span className="text-sm font-medium text-gray-900">{entry.value}</span>
                        </div>
                    ))}
                </div>
            </div>
        )
    }
    return null
}

const ChartTooltipContent = ChartTooltip

export { ChartContainer, ChartTooltip, ChartTooltipContent }

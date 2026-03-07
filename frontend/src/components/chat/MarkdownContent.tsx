'use client'

import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { cn } from '@/lib/utils'

interface MarkdownContentProps {
  content: string
  className?: string
}

export function MarkdownContent({ content, className }: MarkdownContentProps) {
  return (
    <div className={cn('text-sm leading-relaxed', className)}>
    <ReactMarkdown
      remarkPlugins={[remarkGfm]}
      components={{
        h1: ({ children }) => (
          <h1 className="text-base font-bold mt-4 mb-3 first:mt-0 flex items-center gap-2 border-b border-primary/20 pb-2">
            {children}
          </h1>
        ),
        h2: ({ children }) => (
          /* Day headers rendered as visual timeline cards */
          <div className="mt-4 mb-2 first:mt-0">
            <div className="inline-flex items-center gap-2 bg-gradient-to-r from-teal-600 to-emerald-600 text-white text-sm font-bold px-4 py-1.5 rounded-full shadow-sm">
              {children}
            </div>
          </div>
        ),
        h3: ({ children }) => (
          <h3 className="text-[13px] font-semibold mt-3 mb-1 first:mt-0 text-teal-700 border-l-2 border-teal-400 pl-2">
            {children}
          </h3>
        ),
        p: ({ children }) => (
          <p className="mb-2 last:mb-0">{children}</p>
        ),
        ul: ({ children }) => (
          <ul className="list-none pl-0 mb-2 space-y-1">{children}</ul>
        ),
        ol: ({ children }) => (
          <ol className="list-decimal pl-4 mb-2 space-y-0.5">{children}</ol>
        ),
        li: ({ children }) => (
          <li className="text-sm flex gap-1.5 items-start">
            <span className="mt-1 shrink-0 w-1.5 h-1.5 rounded-full bg-teal-400 inline-block" />
            <span>{children}</span>
          </li>
        ),
        strong: ({ children }) => (
          <strong className="font-semibold text-foreground">{children}</strong>
        ),
        em: ({ children }) => (
          <em className="text-muted-foreground not-italic text-xs">{children}</em>
        ),
        a: ({ href, children }) => (
          <a href={href} target="_blank" rel="noopener noreferrer" className="text-primary underline hover:no-underline">
            {children}
          </a>
        ),
        table: ({ children }) => (
          <div className="overflow-x-auto mb-3 rounded-lg border border-border/60 shadow-sm">
            <table className="w-full text-xs border-collapse">{children}</table>
          </div>
        ),
        thead: ({ children }) => (
          <thead className="bg-teal-50 border-b border-border/60">{children}</thead>
        ),
        th: ({ children }) => (
          <th className="px-3 py-2 text-left font-semibold text-teal-800">{children}</th>
        ),
        td: ({ children }) => (
          <td className="border-t border-border/40 px-3 py-1.5 text-muted-foreground">{children}</td>
        ),
        code: ({ children, className: codeClassName }) => {
          const isInline = !codeClassName
          return isInline ? (
            <code className="bg-muted px-1 py-0.5 rounded text-xs font-mono">{children}</code>
          ) : (
            <pre className="bg-muted rounded-lg p-3 overflow-x-auto mb-2">
              <code className="text-xs font-mono">{children}</code>
            </pre>
          )
        },
        blockquote: ({ children }) => (
          <blockquote className="bg-teal-50/60 border-l-3 border-teal-400 pl-3 pr-2 py-1.5 rounded-r-lg text-sm text-teal-900 mb-2">
            {children}
          </blockquote>
        ),
        hr: () => <hr className="my-4 border-border/40" />,
      }}
    >
      {content}
    </ReactMarkdown>
    </div>
  )
}

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
          <h1 className="text-lg font-bold mt-4 mb-2 first:mt-0">{children}</h1>
        ),
        h2: ({ children }) => (
          <h2 className="text-base font-bold mt-3 mb-1.5 first:mt-0">{children}</h2>
        ),
        h3: ({ children }) => (
          <h3 className="text-sm font-semibold mt-2 mb-1 first:mt-0">{children}</h3>
        ),
        p: ({ children }) => (
          <p className="mb-2 last:mb-0">{children}</p>
        ),
        ul: ({ children }) => (
          <ul className="list-disc pl-4 mb-2 space-y-0.5">{children}</ul>
        ),
        ol: ({ children }) => (
          <ol className="list-decimal pl-4 mb-2 space-y-0.5">{children}</ol>
        ),
        li: ({ children }) => (
          <li className="text-sm">{children}</li>
        ),
        strong: ({ children }) => (
          <strong className="font-semibold">{children}</strong>
        ),
        a: ({ href, children }) => (
          <a href={href} target="_blank" rel="noopener noreferrer" className="text-primary underline hover:no-underline">
            {children}
          </a>
        ),
        table: ({ children }) => (
          <div className="overflow-x-auto mb-2">
            <table className="w-full text-xs border-collapse">{children}</table>
          </div>
        ),
        thead: ({ children }) => (
          <thead className="bg-muted/50">{children}</thead>
        ),
        th: ({ children }) => (
          <th className="border border-border px-2 py-1 text-left font-semibold">{children}</th>
        ),
        td: ({ children }) => (
          <td className="border border-border px-2 py-1">{children}</td>
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
          <blockquote className="border-l-2 border-primary/30 pl-3 italic text-muted-foreground mb-2">
            {children}
          </blockquote>
        ),
        hr: () => <hr className="my-3 border-border" />,
      }}
    >
      {content}
    </ReactMarkdown>
    </div>
  )
}

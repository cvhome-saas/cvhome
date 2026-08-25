import type {ReactNode} from 'react';
import '../globals.css';

/** Root layout for system pages that have no store (and therefore no theme and no locale). */
export default function SystemLayout({children}: { children: ReactNode }) {
    return (
        <html lang="en" data-theme="system">
        <body className="min-h-dvh bg-background text-foreground">{children}</body>
        </html>
    );
}

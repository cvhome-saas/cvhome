'use client'
import {useThemeStates} from '@/shell/theme/theme-client-states';

/** The `[locale]` error boundary. Theme-agnostic: the root layout hands the theme's states down by context. */
export default function StorefrontError({error, reset}: { error: Error & { digest?: string }; reset: () => void }) {
    const {ErrorState} = useThemeStates();
    return <ErrorState error={error} reset={reset}/>;
}

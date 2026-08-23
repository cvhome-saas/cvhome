'use client'
import {useThemeStates} from '@/shell/theme/theme-client-states';

export default function StorefrontError({error, reset}: { error: Error & { digest?: string }; reset: () => void }) {
    const {ErrorState} = useThemeStates();
    return <ErrorState error={error} reset={reset}/>;
}

import {getTheme} from '@/shell/theme/get-theme';
import {policyPage} from '@/shell/routes/policy';

export {generateMetadata} from '@/shell/routes/policy';
export default policyPage(getTheme);

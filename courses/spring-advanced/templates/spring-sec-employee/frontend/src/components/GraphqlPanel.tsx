import { useState } from 'react';
import { graphql } from '../services/api';
import { useTask } from './useTask';
import { Feedback } from './Feedback';
export default function GraphqlPanel() {
  const [query, setQuery] = useState('query {\n  departments { id departmentName employees { id employeeName } }\n}');
  const [variables, setVariables] = useState('{}');
  const [result, setResult] = useState<unknown>(null);
  const task = useTask();
  return <section className="panel"><h2>GraphQL クエリエディター</h2>
    <p>必要な項目だけ選択して実行できます。Mutationも実際にデータを変更します。</p>
    <label>Query / Mutation<textarea rows={7} value={query} onChange={(event) => setQuery(event.target.value)} /></label>
    <label>Variables（JSON）<textarea rows={2} value={variables} onChange={(event) => setVariables(event.target.value)} /></label>
    <button disabled={task.busy} onClick={() => void task.run(async () => {
      const parsed: unknown = JSON.parse(variables);
      if (typeof parsed !== 'object' || parsed === null || Array.isArray(parsed)) throw new Error('VariablesはJSONオブジェクトにしてください');
      setResult(await graphql(query, parsed as Record<string, unknown>));
    })}>クエリ実行</button><Feedback {...task} /><pre>{JSON.stringify(result, null, 2)}</pre>
  </section>;
}

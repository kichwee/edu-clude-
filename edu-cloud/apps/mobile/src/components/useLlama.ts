import { useState } from 'react';
import { LlamaContext } from 'llama.rn';
import { Platform } from 'react-native';

const MODEL_PATH = Platform.OS === 'android' 
  ? '/data/user/0/com.educloud/files/qwen2-0_5b.Q4_K_M.gguf'
  : 'qwen2-0_5b.Q4_K_M.gguf';

export const useLlama = () => {
  const [context, setContext] = useState<LlamaContext | null>(null);
  const [isInitializing, setIsInitializing] = useState(false);

  const initModel = async () => {
    setIsInitializing(true);
    try {
      const ctx = await LlamaContext.create({
        model: MODEL_PATH,
        contextSize: 2048,
        batchSize: 512,
        threads: 4, // Respect 2GB device limits, keep CPU threads moderate
        useCoreML: false, 
        useMetal: false
      });
      setContext(ctx);
    } catch (e) {
      console.error('Llama initialization failed:', e);
    } finally {
      setIsInitializing(false);
    }
  };

  const generate = async (prompt: string, onToken?: (token: string) => void) => {
    if (!context) throw new Error('Model not initialized');
    
    let fullResponse = '';
    
    // llama.rn completion stream
    const result = await context.completion(
      {
        prompt,
        n_predict: 256,
        temperature: 0.2, // Low temp for factual tutoring
        top_p: 0.85,
        stop: ['<|im_end|>', 'User:', 'Student:']
      },
      (data) => {
        const token = data.token;
        fullResponse += token;
        if (onToken) onToken(token);
      }
    );
    
    return fullResponse;
  };

  return { context, isInitializing, initModel, generate };
};

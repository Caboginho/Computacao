# src/model/score_manager.py

import json
import os
import time
from utils.constants import SCORE_FILE_PATH

class ScoreManager:
    def __init__(self, max_scores=3):
        self.max_scores = max_scores
        self.file_path = SCORE_FILE_PATH
        os.makedirs(os.path.dirname(self.file_path), exist_ok=True)
        self._init_file()

    def _init_file(self):
        if not os.path.exists(self.file_path):
            with open(self.file_path, "w", encoding="utf-8") as f:
                json.dump([], f)

    def load_scores(self):
        try:
            with open(self.file_path, "r", encoding="utf-8") as f:
                scores = json.load(f)
                # converte números antigos em dict
                for i, s in enumerate(scores):
                    if isinstance(s, int):
                        scores[i] = {"score": s, "tempo": 0, "data": time.strftime("%d/%m/%Y %H:%M")}
                scores.sort(key=lambda x: (-x['score'], x['tempo']))
                return scores[:self.max_scores]
        except (json.JSONDecodeError, FileNotFoundError):
            return []

    def save_score(self, score, tempo):
        scores = self.load_scores()
        novo_score = {
            'score': score,
            'tempo': tempo,
            'data': time.strftime("%d/%m/%Y %H:%M")
        }
        scores.append(novo_score)
        scores.sort(key=lambda x: (-x['score'], x['tempo']))
        scores = scores[:self.max_scores]
        with open(self.file_path, "w", encoding="utf-8") as f:
            json.dump(scores, f, indent=4)
        return scores

    def best_scores(self):
        return self.load_scores()

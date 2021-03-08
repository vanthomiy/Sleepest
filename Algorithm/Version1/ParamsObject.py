from typing import List


class Parameter:
    art: str
    min: int
    max: int
    set: int
    offset: int
    faktor: int

    def __init__(self, art: str, min: int, max: int, set: int, offset: int, faktor: int) -> None:
        self.art = art
        self.min = min
        self.max = max
        self.set = set
        self.offset = offset
        self.faktor = faktor


class ParamsObject:
    start_parameter: List[Parameter]
    actual_parameter: List[Parameter]

    def __init__(self, start_parameter: List[Parameter], actual_parameter: List[Parameter]) -> None:       
        self.start_parameter = start_parameter
        self.actual_parameter = actual_parameter





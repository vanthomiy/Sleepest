

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







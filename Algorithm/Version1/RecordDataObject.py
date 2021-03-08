class RecordDataObject(object):
    def __init__(self, name=None, date=None, time=None, sleep=None, light=None, motion=None, realSleep=None):
        self.name = name
        self.date = date
        self.time = time
        self.sleep = sleep
        self.light = light
        self.motion = motion
        self.realSleep = realSleep

        
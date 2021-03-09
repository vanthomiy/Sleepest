class RecordDataObject(object):
    def __init__(self, date=None, time=None, sleep=None, light=None, motion=None, realSleep=None):
        self.date = date
        self.time = time
        self.sleep = sleep-1
        self.light = light-1
        self.motion = motion
        self.realSleep = realSleep

        
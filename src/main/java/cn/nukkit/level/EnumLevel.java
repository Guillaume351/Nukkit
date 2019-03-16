package cn.nukkit.level;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EnumLevel {
    OVERWORLD(""),
    NETHER("nether"),
    END("end");

    @Getter
    private final String suffix;

    @Getter
    private final int id = ordinal();

    public static Position moveToNether(Position current) {
        Level target;

        switch (current.level.getDimension()) {
            case OVERWORLD:
                target = current.level.getNether();
                return new Position(mRound(current.getFloorX() >> 3, 128), mRound(current.getFloorY(), 32), mRound(current.getFloorZ() >> 3, 128), target);
            case NETHER:
                target = current.level.getOverworld();
                return new Position(mRound(current.getFloorX() << 3, 1024), mRound(current.getFloorY(), 32), mRound(current.getFloorZ() << 3, 1024), target);
            default:
                return null;
        }
    }

    private static int mRound(int value, int factor) {
        return Math.round(value / factor) * factor;
    }
}

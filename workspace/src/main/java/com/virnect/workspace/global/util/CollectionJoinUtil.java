package com.virnect.workspace.global.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Project        : PF-Workspace
 * DATE           : 2022-12-08
 * AUTHOR         : VIRNECT (Jintae Kim)
 * EMAIL          : jtkim@virnect.com
 * DESCRIPTION    :
 * ===========================================================
 * DATE            AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2022-12-08      VIRNECT          최초 생성
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CollectionJoinUtil {
	public static <T, U> CollectionJoiner<T, U> collections(Collection<T> t, Collection<U> u) {
		return new CollectionJoiner<>(t, u);
	}

	public static class CollectionJoiner<T, U> {
		private Collection<T> t;
		private Collection<U> u;
		private BiPredicate<T, U> condition;

		public CollectionJoiner(Collection<T> t, Collection<U> u) {
			this.t = t;
			this.u = u;
		}

		public CollectionJoiner<T, U> when(BiPredicate<T, U> condition) {
			this.condition = condition;
			return this;
		}

		public <R> List<R> then(BiFunction<T, U, R> function) {
			List<R> list = new ArrayList<>();
			for (T tKey : t) {
				for (U uKey : u) {
					if (condition.test(tKey, uKey)) {
						list.add(function.apply(tKey, uKey));
					}
				}
			}
			return list;
		}
	}
}

package com.github.ruediste.salta.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.reflect.TypeToken;

public class StaticBindingSet {
	private HashMap<TypeToken<?>, List<StaticBinding>> staticBindingMap = new HashMap<>();
	private ArrayList<StaticBinding> nonTypeSpecificStaticBindings = new ArrayList<>();

}

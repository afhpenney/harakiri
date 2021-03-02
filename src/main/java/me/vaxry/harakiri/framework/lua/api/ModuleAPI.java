package me.vaxry.harakiri.framework.lua.api;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.module.Module;
import me.vaxry.harakiri.framework.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

public class ModuleAPI extends TwoArgFunction {

    public static ModuleAPI MODULEAPI = null;

    public ModuleAPI() { MODULEAPI = this; }

    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable module = new LuaTable(0,30);
        module.set( "toggle", new toggle() );
        module.set( "setEnabled", new setEnabled() );
        module.set( "setValue", new setValue() );
        env.set( "module", module );
        env.get("package").get("loaded").set("module", module);
        return module;
    }

    protected static class toggle extends OneArgFunction {
        public LuaValue call(LuaValue modulename){
            try {
                Harakiri.INSTANCE.getModuleManager().find(modulename.toString()).toggle();
            }catch(Throwable t){
                return LuaValue.valueOf(0);
            }
            return LuaValue.valueOf(1);
        }
    }

    protected static class setEnabled extends TwoArgFunction {
        public LuaValue call(LuaValue modulename, LuaValue enabled){
            try {
                Module mod = Harakiri.INSTANCE.getModuleManager().find(modulename.toString());
                if(mod.isEnabled() == enabled.checkboolean()) return LuaValue.valueOf(1);

                mod.toggle();

            }catch(Throwable t){
                return LuaValue.valueOf(0);
            }
            return LuaValue.valueOf(1);
        }
    }

    protected static class setValue extends VarArgFunction {
        public Varargs invoke(Varargs args) {
            String module = args.arg(1).checkjstring();
            String value = args.arg(2).checkjstring();

            try {
                Value realVal = Harakiri.INSTANCE.getModuleManager().find(module).findValue(value);
                if(realVal.getValue() instanceof Boolean){
                    realVal.setValue(args.arg(3).checkboolean());
                }else if(realVal.getValue() instanceof Integer){
                    realVal.setValue(args.arg(3).checkint());
                }else if(realVal.getValue() instanceof Float){
                    realVal.setValue((float)args.arg(3).checkdouble());
                }else if(realVal.getValue() instanceof Double){
                    realVal.setValue(args.arg(3).checkdouble());
                }else{
                    LuaValue.valueOf(0);
                }
            }catch(Throwable t){
                Harakiri.INSTANCE.logChat(t.toString());
                return LuaValue.valueOf(0);
            }
            return LuaValue.valueOf(1);
        }
    }

    protected static class getValue extends TwoArgFunction {
        public LuaValue call(LuaValue modulename, LuaValue value){
            try {
                Value realVal = Harakiri.INSTANCE.getModuleManager().find(modulename.toString()).findValue(value.toString());
                if(realVal.getValue() instanceof Boolean){
                    return LuaValue.valueOf(Boolean.valueOf((Boolean)realVal.getValue()));
                }else if(realVal.getValue() instanceof Integer){
                    return LuaValue.valueOf(Integer.valueOf((Integer)realVal.getValue()));
                }else if(realVal.getValue() instanceof Float){
                    return LuaValue.valueOf(Float.valueOf((Float)realVal.getValue()));
                }else if(realVal.getValue() instanceof Double){
                    return LuaValue.valueOf(Double.valueOf((Double)realVal.getValue()));
                }else{
                    return LuaValue.NIL;
                }
            }catch(Throwable t){
                return LuaValue.NIL;
            }
        }
    }
}

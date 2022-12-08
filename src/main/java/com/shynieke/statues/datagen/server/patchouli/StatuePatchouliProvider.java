//package com.shynieke.statues.datagen.server.patchouli;
//
//import com.shynieke.statues.Reference;
//import com.shynieke.statues.registry.StatueRegistry;
//import net.minecraft.data.DataGenerator;
//import net.minecraft.data.PackOutput;
//import net.minecraft.world.item.ItemStack;
//import xyz.brassgoggledcoders.patchouliprovider.BookBuilder;
//import xyz.brassgoggledcoders.patchouliprovider.PatchouliBookProvider;
//
//import java.util.function.Consumer;
//
//public class StatuePatchouliProvider extends PatchouliBookProvider {
//	public StatuePatchouliProvider(PackOutput output) {
//		super(output, Reference.MOD_ID, "en_us");
//	}
//
//	@Override
//	protected void addBooks(Consumer<BookBuilder> consumer) {
//		BookBuilder bookBuilder = createBookBuilder("statues",
//				"info.statues.book.name", "info.statues.book.landing")
//				.setSubtitle("info.statues.book.subtitle")
//				.setCreativeTab("statues.items")
//				.setModel("patchouli:book_gray")
//				.setBookTexture("patchouli:textures/gui/book_gray.png")
//				.setShowProgress(false)
//				.setUseBlockyFont(true)
//				.setI18n(true)
//				.addMacro("$(item)", "$(#c47567)")
//				.setUseResourcePack(false);
//
//		//Info category
//		bookBuilder = bookBuilder.addCategory("info", "info.statues.book.info.name",
//						"info.statues.book.info.desc", "statues:info_statue")
//
//				//Add Statue Core entry
//				.addEntry("info/core", "info.statues.book.core.entry.name", StatueRegistry.STATUE_CORE.getId().toString())
//				.addTextPage("info.statues.book.core.text1").build()
//				.addEntityPage("statues:statue_bat").build()
//				.addSpotlightPage(new ItemStack(StatueRegistry.STATUE_CORE.get())).build()
//				.build()
//
//				//Add S.T.A.T.U.E. entry
//				.addEntry("info/statue_table", "info.statues.book.statue_table.entry.name", StatueRegistry.STATUE_TABLE.getId().toString())
//				.addTextPage("info.statues.book.statue_table.text1").build()
//				.addSpotlightPage(new ItemStack(StatueRegistry.STATUE_TABLE.get()))
//				.setText("info.statues.book.statue_table.text2").build()
//				.addTextPage("info.statues.book.statue_table.text3").build()
//				.addTextPage("info.statues.book.statue_table.text4").build()
//				.build()
//
//				.build(); //Back to the bookbuilder
//
//
//		//Finish book
//		bookBuilder.build(consumer);
//	}
//}
